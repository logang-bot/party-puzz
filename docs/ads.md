# Ads

The app monetises with Google AdMob. Three ad formats are integrated: banner ads on informational screens, an App Open Ad on every app launch / foreground return, and a rewarded ad that unlocks premium question packs.

---

## Setup

### Dependency

```toml
# gradle/libs.versions.toml
playServicesAds = "23.6.0"
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
```

### Manifest

The App ID is a manifest placeholder rather than a literal, so debug and staging get Google's sample
app id and only `release` carries the real one (`app/build.gradle.kts`, per-build-type
`manifestPlaceholders`):

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="${admobAppId}" />
```

### Initialisation

`MobileAds.initialize(this)` is called in `PartyPuzlApplication.onCreate()` — the earliest possible point in the app lifecycle, before any Activity exists.

---

## Ad Unit IDs

All IDs live in `AdUnitIds` inside `AdBannerView.kt`.

| Constant | Format | Screen |
|---|---|---|
| `HOME_BANNER` | Banner | `HomeScreen` |
| `PARTIES_BANNER` | Banner | `PartiesScreen` |
| `PARTY_DETAIL_BANNER` | Banner | `PartyDetailScreen` |
| `SETTINGS_BANNER` | Banner | `SettingsScreen` |
| `GAME_CONFIG_BANNER` | Banner | `GameConfigScreen` |
| `APP_OPEN` | App Open | App launch / foreground |
| `PACK_UNLOCK_REWARDED` | Rewarded | `GameConfigScreen` — premium pack unlock |

Banner and App Open constants have real production IDs alongside Google's test IDs, selected by `BuildConfig.USE_TEST_ADS`. `PACK_UNLOCK_REWARDED` is still on the test ID in both build types — see the TODO under [Rewarded Ad](#rewarded-ad).

---

## Banner Ads

### Composable

`AdBannerView(adUnitId, modifier)` in `ui/common/AdBannerView.kt` wraps an `AdView` via `AndroidView`. It calls `loadAd()` once on first composition.

### Placement

| Screen | Position | How |
|---|---|---|
| `HomeScreen` | Pinned at the bottom of the screen | `Alignment.BottomCenter` inside the root `Box`; scrollable `Column` gets `padding(bottom = 50.dp)` |
| `PartiesScreen` | Pinned at the bottom | Same overlay pattern; `LazyColumn` uses `contentPadding = PaddingValues(bottom = 50.dp)` |
| `PartyDetailScreen` | Pinned at the bottom | Same overlay pattern; scrollable `Column` padding adjusted to `bottom = 50.dp` |
| `SettingsScreen` | Pinned at the bottom | Root `Column` wrapped in a `Box`; `padding(bottom = 50.dp)` on the inner `Column` |
| `GameConfigScreen` | Inline, between the mode header and the players section | Placed directly inside the scrollable `Column`, not as an overlay |

The standard banner height is 50 dp (`AdSize.BANNER`).

---

## App Open Ad

Shown every time the app is cold-started or brought back from the background.

### Manager

`AppOpenAdManager` (`ui/common/AppOpenAdManager.kt`) is a Hilt `@Singleton`.

| Method | Purpose |
|---|---|
| `loadAd()` | Starts an async `AppOpenAd.load()`. No-ops if an ad is already available or a load is in progress. |
| `showWhenReady(activity)` | Shows immediately if an ad is loaded and fresh (< 4 h old). Otherwise stores a `WeakReference<Activity>` and shows the instant `onAdLoaded` fires. |
| `clearPendingActivity()` | Drops the stored reference. Called from `MainActivity.onStop()` to prevent a stale Activity from being shown an ad after the app backgrounds. |

`isAdVisible` is a public `mutableStateOf(Boolean)` that Compose can observe.

### Lifecycle wiring (`MainActivity`)

```
Application.onCreate()  →  MobileAds.initialize() + appOpenAdManager.loadAd()
                            (ad starts loading before any Activity exists)

MainActivity.onStart()  →  appOpenAdManager.showWhenReady(this)
MainActivity.onStop()   →  appOpenAdManager.clearPendingActivity()
```

### Full-screen overlay

When the App Open Ad shows, the SDK hides the status bar, which would cause the underlying app content to shift and become visible at the screen edges. To prevent this, `MainActivity.setContent` renders a `Box` that fills the entire screen (including system bar areas, thanks to `enableEdgeToEdge()`) using `MaterialTheme.colorScheme.background` whenever `isAdVisible` is `true`.

```
AdMob App Open Ad window          ← SDK overlay, full-screen
────────────────────────────────────────────────────────────
colorScheme.background Box        ← fillMaxSize, hides app content
HomeNavigation                    ← invisible while ad is showing
```

After the ad is dismissed `isAdVisible` returns to `false` and the overlay is removed.

---

## Rewarded Ad

`RewardedAdCard.kt` (`ui/common/`) contains the rewarded ad system.

| Class / function | Purpose |
|---|---|
| `RewardedAdState` | Holds the loaded `RewardedAd`; exposes `isReady` and `show(activity, onRewarded)` |
| `rememberRewardedAd(adUnitId)` | Composable that creates and remembers a `RewardedAdState`, loading the ad on first composition |
| `RewardedAdCard(rewardedAdState)` | Styled card composable matching the in-app native ad design |

The card shows a fire icon, title (`rewarded_ad_title`), subtitle (`rewarded_ad_subtitle`), and a **WATCH** button that is enabled only once the ad has loaded. Tapping it shows the rewarded video; the `onRewarded` callback is the hook for granting the reward in-game.

### Where it is used

`GameConfigScreen` calls `rememberRewardedAd(AdUnitIds.PACK_UNLOCK_REWARDED)` on composition — up front, not when the sheet opens, so the option is usually ready by the time the user asks for it. `UnlockChoiceBottomSheet` offers it as "Watch a short ad"; granting the reward unlocks that premium question pack **for the session only** via `SessionUnlocksSource`. See [question-packs.md](question-packs.md).

`RewardedAdState.show()` consumes the ad, so the screen reloads it in the reward callback and a second pack can be unlocked in the same session. While a load is in flight the sheet keeps the option visible but disabled with a "Loading ad…" subtitle.

`RewardedAdCard` — the standalone card composable — is still unused; the unlock sheet has its own presentation.

> **TODO:** `PACK_UNLOCK_REWARDED` currently points at Google's test rewarded unit in *both* build types. Create a Rewarded ad unit in the AdMob dashboard and paste its id into `AdUnitIds.Production` (`ui/common/AdBannerView.kt`). Until then the unlock flow works end to end in release builds but earns nothing.
>
> This is the only ad placement without a production id. It is tracked as setup step 5 and on the first-release checklist in [release.md](release.md).

---

## Remove Ads — In-App Purchase

The app has a single "Remove Ads" one-time purchase. There is only one app listing on the Play Store; ads are toggled at runtime based on purchase state.

It doubles as the **full unlock**: owning it unlocks every premium question pack permanently. This is deliberate — one SKU, presented in the unlock sheet as "Unlock everything · all packs, no ads", rather than a second product to create and maintain. See [question-packs.md](question-packs.md).

Only `PackTier.PREMIUM` is gated. Official packs are free, and custom packs the user wrote are always unlocked — nothing to sell, so they carry no lock, no badge affordance and never reach the unlock sheet. See [custom-packs.md](custom-packs.md).

### Product ID

`remove_ads` — create this as a **one-time product (INAPP)** in Play Console → Monetize → Products → In-app products.

> **TODO:** Go to Play Console → your app → Monetize → Products → In-app products → Create product. Set the product ID to exactly `remove_ads`, type **One-time**, set the price, and activate it. The billing client will fail silently to find the product until this is done. Tracked as setup step 4 in [release.md](release.md).
>
> `launchPurchaseFlow` returns `false` while the product is missing, and `GameConfigScreen` shows the `unlock_purchase_unavailable` snackbar rather than appearing to do nothing.

### Purchase flow

1. `BillingManager.connect()` is called in `PartyPuzlApplication.onCreate()`.
2. On connection it queries existing purchases and product details from the Play Store.
3. If the user already owns `remove_ads`, `UserPreferencesRepository.setAdFree(true)` is called and the state is persisted in DataStore.
4. The Settings screen exposes a **Remove Ads** row (under the "Purchases" section). Tapping it calls `BillingManager.launchPurchaseFlow(activity)`.
5. On successful purchase, `onPurchasesUpdated` fires, the purchase is acknowledged, and `isAdFree` flips to `true` app-wide immediately.

`launchPurchaseFlow(activity)` returns `false` when the Play Store has no details for the product yet — no billing connection, or the product was never created in Play Console. `GameConfigScreen` surfaces that as a snackbar rather than appearing to do nothing.

### How `isAdFree` propagates

| Layer | Mechanism |
|---|---|
| DataStore | `UserPreferencesRepository.isAdFree: Flow<Boolean>` — source of truth |
| `AppOpenAdManager` | Collects `isAdFree` via a coroutine scope; `loadAd()` and `showWhenReady()` no-op when true |
| Banner composables | `LocalIsAdFree` CompositionLocal provided in `MainActivity`; `AdBannerView` returns early when true |
| Settings screen | Reads `uiState.isAdFree` from `SettingsViewModel`; bottom padding removed when true |
| Question packs | `GameConfigViewModel` treats `isAdFree` as "all premium unlocked", and mirrors it onto the `question_packs` rows once so it survives a reset of the flag |

### Build types

| Build type | `USE_TEST_ADS` | Debuggable | Purpose |
|---|---|---|---|
| `debug` | `true` | Yes | Local development |
| `staging` | `true` | No | QA — release-like but safe (no risk of invalid traffic) |
| `release` | `false` | No | Production |

---

## Key Files

| File | Role |
|---|---|
| `ui/common/AdBannerView.kt` | `AdBannerView` composable + `AdUnitIds` constants |
| `ui/common/AppOpenAdManager.kt` | App Open Ad load / show lifecycle manager |
| `ui/common/RewardedAdCard.kt` | `RewardedAdState`, `rememberRewardedAd`, `RewardedAdCard` |
| `ui/views/gameConfig/ui/UnlockChoiceBottomSheet.kt` | Rewarded-ad vs. purchase choice for premium packs |
| `data/local/appData/appDataSource/SessionUnlocksSource.kt` | In-memory session unlocks earned from rewarded ads |
| `PartyPuzlApplication.kt` | `MobileAds.initialize()` + `appOpenAdManager.loadAd()` on app start |
| `ui/MainActivity.kt` | `showWhenReady` / `clearPendingActivity` hooks + full-screen overlay |
| `AndroidManifest.xml` | `APPLICATION_ID` meta-data |
| `gradle/libs.versions.toml` | `play-services-ads` version entry |
