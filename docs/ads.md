# Ads

The app monetises with Google AdMob. Three ad formats are integrated: banner ads on informational screens, an App Open Ad on every app launch / foreground return, and a rewarded ad infrastructure ready to be wired up.

---

## Setup

### Dependency

```toml
# gradle/libs.versions.toml
playServicesAds = "23.6.0"
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
```

### Manifest

```xml
<!-- TODO: Replace with your production AdMob App ID -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />
```

### Initialisation

`MobileAds.initialize(this)` is called in `PartyPuzzApplication.onCreate()` — the earliest possible point in the app lifecycle, before any Activity exists.

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

All values are currently set to Google's **test IDs**. Replace each constant with the matching production ad unit ID from the AdMob dashboard before releasing.

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
| `GameConfigScreen` | Inline, between the options grid and the players section | Placed directly inside the scrollable `Column`, not as an overlay |

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

## Rewarded Ad (infrastructure)

`RewardedAdCard.kt` (`ui/common/`) contains the full rewarded ad system, ready to be wired into any screen.

| Class / function | Purpose |
|---|---|
| `RewardedAdState` | Holds the loaded `RewardedAd`; exposes `isReady` and `show(activity, onRewarded)` |
| `rememberRewardedAd(adUnitId)` | Composable that creates and remembers a `RewardedAdState`, loading the ad on first composition |
| `RewardedAdCard(rewardedAdState)` | Styled card composable matching the in-app native ad design |

The card shows a fire icon, title (`rewarded_ad_title`), subtitle (`rewarded_ad_subtitle`), and a **WATCH** button that is enabled only once the ad has loaded. Tapping it shows the rewarded video; the `onRewarded` callback is the hook for granting the reward in-game.

> This system is not currently wired into any screen. To activate it, call `rememberRewardedAd(AdUnitIds.GAME_CONFIG_REWARDED)` at the top of the target composable and place `RewardedAdCard(rewardedAdState)` in the layout. Add `GAME_CONFIG_REWARDED` back to `AdUnitIds` with the test rewarded ID `ca-app-pub-3940256099942544/5224354917`.

---

## Key Files

| File | Role |
|---|---|
| `ui/common/AdBannerView.kt` | `AdBannerView` composable + `AdUnitIds` constants |
| `ui/common/AppOpenAdManager.kt` | App Open Ad load / show lifecycle manager |
| `ui/common/RewardedAdCard.kt` | `RewardedAdState`, `rememberRewardedAd`, `RewardedAdCard` |
| `PartyPuzzApplication.kt` | `MobileAds.initialize()` + `appOpenAdManager.loadAd()` on app start |
| `ui/MainActivity.kt` | `showWhenReady` / `clearPendingActivity` hooks + full-screen overlay |
| `AndroidManifest.xml` | `APPLICATION_ID` meta-data |
| `gradle/libs.versions.toml` | `play-services-ads` + `material-icons-extended` version entries |
