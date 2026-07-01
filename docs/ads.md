# Ads

The app monetises with Unity Ads. Three ad formats are integrated: banner ads on informational screens, an interstitial shown on every app launch / foreground return (standing in for AdMob's App Open Ad, which Unity Ads has no equivalent of), and a rewarded ad infrastructure ready to be wired up.

---

## Setup

### Dependency

```toml
# gradle/libs.versions.toml
unityAds = "4.15.1"
unity-ads = { group = "com.unity3d.ads", name = "unity-ads", version.ref = "unityAds" }
```

### Game ID

Unity Ads is initialised with an **Android Game ID** from the Unity Dashboard (Monetization → your project → the "Android" platform entry), passed via `BuildConfig.UNITY_GAME_ID`. This app's Game ID is `800079402`, set as a `buildConfigField` on all three build types in `app/build.gradle.kts`.

Unlike AdMob, Unity Ads doesn't need any `AndroidManifest.xml` meta-data — the SDK is initialised entirely in code. It does require one manifest permission, since the app targets Android 13+:

```xml
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

### Initialisation

`UnityAds.initialize(this, BuildConfig.UNITY_GAME_ID, BuildConfig.USE_TEST_ADS, listener)` is called in `PartyPuzlApplication.onCreate()` — the earliest possible point in the app lifecycle, before any Activity exists. The App Open interstitial's first `loadAd()` call is chained off `onInitializationComplete()` so it never races SDK init.

---

## Ad Placement IDs

All IDs live in `AdPlacementIds` inside `AdBannerView.kt`.

| Constant | Placement ID | Format | Screen |
|---|---|---|---|
| `HOME_BANNER` | `Banner_Android` | Banner | `HomeScreen` |
| `PARTIES_BANNER` | `Banner_Android` | Banner | `PartiesScreen` |
| `PARTY_DETAIL_BANNER` | `Banner_Android` | Banner | `PartyDetailScreen` |
| `SETTINGS_BANNER` | `Banner_Android` | Banner | `SettingsScreen` |
| `GAME_CONFIG_BANNER` | `Banner_Android` | Banner | `GameConfigScreen` |
| `APP_OPEN_INTERSTITIAL` | `Interstitial_Android` | Interstitial | App launch / foreground |
| `GAME_CONFIG_REWARDED` | `Rewarded_Android` | Rewarded | Not wired in yet — see [Rewarded Ad](#rewarded-ad-infrastructure) |

All five banner screens share the single `Banner_Android` placement — Unity Ads placements aren't scoped per-screen the way AdMob ad units were, so per-screen breakdowns in the Unity Dashboard use its own analytics (impression source), not separate placement IDs. `Banner_Android`, `Interstitial_Android`, and `Rewarded_Android` are the default placements Unity auto-creates per format for a new Android app entry.

Unlike AdMob, Unity Ads doesn't use separate test/production ad unit IDs — the same placement ID is used everywhere, and `BuildConfig.USE_TEST_ADS` instead controls the `testMode` flag passed to `UnityAds.initialize()`, which routes all placements through Unity's test ad network.

---

## Banner Ads

### Composable

`AdBannerView(placementId, modifier)` in `ui/common/AdBannerView.kt` wraps a Unity `BannerView` via `AndroidView`. It calls `load()` once on first composition and `destroy()` when the view leaves composition.

### Placement

| Screen | Position | How |
|---|---|---|
| `HomeScreen` | Pinned at the bottom of the screen | `Alignment.BottomCenter` inside the root `Box`; scrollable `Column` gets `padding(bottom = 50.dp)` |
| `PartiesScreen` | Pinned at the bottom | Same overlay pattern; `LazyColumn` uses `contentPadding = PaddingValues(bottom = 50.dp)` |
| `PartyDetailScreen` | Pinned at the bottom | Same overlay pattern; scrollable `Column` padding adjusted to `bottom = 50.dp` |
| `SettingsScreen` | Pinned at the bottom | Root `Column` wrapped in a `Box`; `padding(bottom = 50.dp)` on the inner `Column` |
| `GameConfigScreen` | Inline, between the options grid and the players section | Placed directly inside the scrollable `Column`, not as an overlay |

The banner size is a fixed 320×50 (`UnityBannerSize(320, 50)`), matching AdMob's old standard banner footprint.

---

## App Open Interstitial

Shown every time the app is cold-started or brought back from the background, standing in for AdMob's App Open Ad format (Unity Ads has no dedicated app-open placement type).

### Manager

`AppOpenAdManager` (`ui/common/AppOpenAdManager.kt`) is a Hilt `@Singleton`.

| Method | Purpose |
|---|---|
| `loadAd()` | Starts an async `UnityAds.load()` on `AdPlacementIds.APP_OPEN_INTERSTITIAL`. No-ops if an ad is already available or a load is in progress. |
| `showWhenReady(activity)` | Shows immediately if an ad is loaded and fresh (< 4 h old). Otherwise stores a `WeakReference<Activity>` and shows the instant `onUnityAdsAdLoaded` fires. |
| `clearPendingActivity()` | Drops the stored reference. Called from `MainActivity.onStop()` to prevent a stale Activity from being shown an ad after the app backgrounds. |

`isAdVisible` is a public `mutableStateOf(Boolean)` that Compose can observe.

### Lifecycle wiring (`MainActivity`)

```
Application.onCreate()  →  UnityAds.initialize() → onInitializationComplete() → appOpenAdManager.loadAd()
                            (ad starts loading once the SDK finishes initialising)

MainActivity.onStart()  →  appOpenAdManager.showWhenReady(this)
MainActivity.onStop()   →  appOpenAdManager.clearPendingActivity()
```

### Full-screen overlay

When the interstitial shows, the SDK renders its own full-screen Activity, but the underlying Compose content briefly shows a frame edge-to-edge before/after that transition. To prevent this, `MainActivity.setContent` renders a `Box` that fills the entire screen (including system bar areas, thanks to `enableEdgeToEdge()`) using `MaterialTheme.colorScheme.background` whenever `isAdVisible` is `true`.

```
Unity Ads interstitial window     ← SDK overlay, full-screen
────────────────────────────────────────────────────────────
colorScheme.background Box        ← fillMaxSize, hides app content
HomeNavigation                    ← invisible while ad is showing
```

After the ad is dismissed `isAdVisible` returns to `false` and the overlay is removed.

---

## Rewarded Ad (infrastructure)

`RewardedAdCard.kt` (`ui/common/`) contains the full rewarded ad system, ready to be wired into any screen.

| Class / function | Purpose |
|---|---|
| `RewardedAdState` | Loads a Unity rewarded placement; exposes `isReady` and `show(activity, onRewarded)` |
| `rememberRewardedAd(placementId)` | Composable that creates and remembers a `RewardedAdState`, loading the ad on first composition |
| `RewardedAdCard(rewardedAdState)` | Styled card composable matching the in-app native ad design |

The card shows a fire icon, title (`rewarded_ad_title`), subtitle (`rewarded_ad_subtitle`), and a **WATCH** button that is enabled only once the ad has loaded. Tapping it shows the rewarded video; `onRewarded` fires only when Unity reports `UnityAdsShowCompletionState.COMPLETED` (i.e. the user watched to the end) — the hook for granting the reward in-game.

> This system is not currently wired into any screen. The `Rewarded_Android` placement is already configured in `AdPlacementIds.GAME_CONFIG_REWARDED`. To activate it, call `rememberRewardedAd(AdPlacementIds.GAME_CONFIG_REWARDED)` at the top of the target composable and place `RewardedAdCard(rewardedAdState)` in the layout.

---

## Remove Ads — In-App Purchase

The app has a single "Remove Ads" one-time purchase. There is only one app listing on the Play Store; ads are toggled at runtime based on purchase state. This is unaffected by the Unity Ads migration.

### Product ID

`remove_ads` — create this as a **one-time product (INAPP)** in Play Console → Monetize → Products → In-app products.

> **TODO:** Go to Play Console → your app → Monetize → Products → In-app products → Create product. Set the product ID to exactly `remove_ads`, type **One-time**, set the price, and activate it. The billing client will fail silently to find the product until this is done.

### Purchase flow

1. `BillingManager.connect()` is called in `PartyPuzlApplication.onCreate()`.
2. On connection it queries existing purchases and product details from the Play Store.
3. If the user already owns `remove_ads`, `UserPreferencesRepository.setAdFree(true)` is called and the state is persisted in DataStore.
4. The Settings screen exposes a **Remove Ads** row (under the "Purchases" section). Tapping it calls `BillingManager.launchPurchaseFlow(activity)`.
5. On successful purchase, `onPurchasesUpdated` fires, the purchase is acknowledged, and `isAdFree` flips to `true` app-wide immediately.

### How `isAdFree` propagates

| Layer | Mechanism |
|---|---|
| DataStore | `UserPreferencesRepository.isAdFree: Flow<Boolean>` — source of truth |
| `AppOpenAdManager` | Collects `isAdFree` via a coroutine scope; `loadAd()` and `showWhenReady()` no-op when true |
| Banner composables | `LocalIsAdFree` CompositionLocal provided in `MainActivity`; `AdBannerView` returns early when true |
| Settings screen | Reads `uiState.isAdFree` from `SettingsViewModel`; bottom padding removed when true |

### Build types

| Build type | `USE_TEST_ADS` | Debuggable | Purpose |
|---|---|---|---|
| `debug` | `true` | Yes | Local development |
| `staging` | `true` | No | QA — release-like but safe (routes through Unity's test ad network) |
| `release` | `false` | No | Production |

---

## Key Files

| File | Role |
|---|---|
| `ui/common/AdBannerView.kt` | `AdBannerView` composable + `AdPlacementIds` constants |
| `ui/common/AppOpenAdManager.kt` | App-open interstitial load / show lifecycle manager |
| `ui/common/RewardedAdCard.kt` | `RewardedAdState`, `rememberRewardedAd`, `RewardedAdCard` |
| `PartyPuzlApplication.kt` | `UnityAds.initialize()` + `appOpenAdManager.loadAd()` on app start |
| `ui/MainActivity.kt` | `showWhenReady` / `clearPendingActivity` hooks + full-screen overlay |
| `gradle/libs.versions.toml` | `unity-ads` version entry |
| `app/build.gradle.kts` | `UNITY_GAME_ID` / `USE_TEST_ADS` `buildConfigField`s per build type |
