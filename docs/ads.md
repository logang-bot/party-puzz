# Ads (AdMob)

The app monetizes via **Google Mobile Ads (AdMob)**. Three ad formats are integrated: Banner, Interstitial, and Rewarded.

---

## Overview

| Format | Where it appears | Status |
|---|---|---|
| Banner | Home screen — pinned at the bottom, always visible | Implemented |
| Interstitial | Game screen — full-screen ad shown when the user confirms exiting a game | Implemented |
| Rewarded | Dare / Sticky Dare skip — user watches a video to skip penalty-free | ⚠️ Manager ready, not yet wired up |

---

## SDK Setup

### Dependency

`play-services-ads 23.3.0` added to `gradle/libs.versions.toml` and `app/build.gradle.kts`.

### Initialization

`MobileAds.initialize(this)` is called in `PartyPuzzApplication.onCreate()` before any ad is loaded. The SDK must be initialized before any `AdView`, `InterstitialAd`, or `RewardedAd` call.

### App ID

The AdMob App ID is declared in `AndroidManifest.xml` as a `<meta-data>` entry under `<application>`:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="..." />
```

> **TODO:** Replace the current test App ID (`ca-app-pub-3940256099942544~3347511713`) with the real one from the AdMob dashboard before publishing. See [Account setup](#account-setup).

### Ad Unit IDs via BuildConfig

Ad unit IDs are stored as `BuildConfig` string fields, set per build type in `app/build.gradle.kts`:

| BuildConfig field | Debug value | Release value |
|---|---|---|
| `BANNER_AD_UNIT_ID` | Google test ID | **TODO: replace** |
| `INTERSTITIAL_AD_UNIT_ID` | Google test ID | **TODO: replace** |
| `REWARDED_AD_UNIT_ID` | Google test ID | **TODO: replace** |
| `ADMOB_APP_ID` | Google test App ID | **TODO: replace** |

> **Never use test IDs in a production build.** The debug block uses Google's official test IDs which are safe for development but earn no revenue and may trigger policy enforcement if shipped.

---

## Account Setup

> **TODO (one-time, before publishing):**
>
> 1. Create an AdMob account at https://admob.google.com
> 2. Click **Add app** → Android → enter package name `com.restrusher.partypuzz`
> 3. Copy the **App ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`)
> 4. Create **3 ad units** — one each for Banner, Interstitial, and Rewarded — and copy their IDs
> 5. Replace the 4 placeholder values in the `release` block of `app/build.gradle.kts`
> 6. Replace the App ID value in `AndroidManifest.xml`

---

## Banner Ad

### Placement

Pinned to `Alignment.BottomCenter` inside the root `Box` of `HomeScreen`. It overlays the screen content and is always visible while on the home screen.

### Implementation

`BannerAdView` is a thin Compose wrapper around the View-based `AdView` using `AndroidView`. AdMob does not have a native Compose component — this bridge is required.

```
HomeScreen (Box)
    └─ Column (game mode pager + party section)
    └─ PartyPickerDialog (conditional)
    └─ BannerAdView ← anchored BottomCenter
```

The ad loads immediately on composition via `AdView.loadAd(AdRequest.Builder().build())`.

---

## Interstitial Ad

### Placement

Shown when the user **confirms** exiting a game. The exit dialog's "Yes" button triggers the ad before navigating back to the home screen.

### Flow

```
User presses back
    └─ showExitDialog = true  ──►  AlertDialog shown

User taps "Yes"
    └─ viewModel.showInterstitial(activity) { onNavigateBack() }
            ├─ Ad loaded  ──►  full-screen ad plays
            │       └─ dismissed or failed  ──►  onNavigateBack() called
            └─ Ad not loaded  ──►  onNavigateBack() called immediately
                    └─ new ad pre-loaded in background
```

### Pre-loading

`InterstitialAdManager` pre-loads the next ad in its `init` block and again immediately after each show (or failed show). By the time the user exits a game, the ad is almost always ready.

### Implementation

`InterstitialAdManager` is a `@Singleton` injected into `GameScreenViewModel`. The composable (`GameScreen`) calls `viewModel.showInterstitial(context as Activity, onDone)` from the exit dialog confirm button.

---

## Rewarded Ad

### Intended placement

> **TODO:** Wire up `RewardedAdManager` to the dare-skip flow in `GameScreenViewModel`:
>
> - `onTruthOrDareSkipped()` — when the player tries to skip after choosing truth or dare
> - `onStickyDareSkipped()` — when the player skips an active sticky dare
>
> **Intended behaviour:**
> 1. Player taps "skip"
> 2. Rewarded ad plays
> 3. If the user watches to completion (`onRewarded` fires) → skip is granted, no punishment applied
> 4. If the ad is dismissed early or unavailable → `modeHandler.applyPunishment(...)` runs as normal
>
> Both functions already have `// TODO` comments marking the exact insertion point.

### Pre-loading

Same pattern as `InterstitialAdManager` — `RewardedAdManager` pre-loads in its `init` block and reloads after each show.

### Implementation

`RewardedAdManager` is a `@Singleton` with `@Inject constructor`. It needs to be injected into `GameScreenViewModel` (not yet done) once the skip flow is wired up.

---

## Ad Managers

Both `InterstitialAdManager` and `RewardedAdManager` follow the same pattern:

| Concern | Detail |
|---|---|
| Scope | `@Singleton` — one instance per app process |
| Context | `@ApplicationContext` — no Activity leak |
| Pre-loading | `loadAd()` called in `init`; reloaded after every show or failed show |
| Fallback | If no ad is loaded when `showAd` is called, the `onDismissed` / `onDone` callback fires immediately so the app never blocks waiting for an ad |
| Ad unit ID | Read from `BuildConfig` at call time — swapped automatically per build type |

---

## Key Files

| File | Role |
|---|---|
| `PartyPuzzApplication.kt` | `MobileAds.initialize(this)` in `onCreate()` |
| `AndroidManifest.xml` | `INTERNET` permission + AdMob App ID `<meta-data>` |
| `app/build.gradle.kts` | `play-services-ads` dependency + `BuildConfig` ad unit ID fields per build type |
| `gradle/libs.versions.toml` | `playServicesAds` version + `play-services-ads` library entry |
| `ui/common/ads/BannerAdView.kt` | Compose `AndroidView` wrapper for `AdView` |
| `ui/common/ads/InterstitialAdManager.kt` | `@Singleton` — pre-loads and shows interstitial ads |
| `ui/common/ads/RewardedAdManager.kt` | `@Singleton` — pre-loads and shows rewarded video ads |
| `ui/views/home/HomeScreen.kt` | Renders `BannerAdView` pinned to the bottom |
| `ui/views/game/gameScreen/GameScreenViewModel.kt` | Injects `InterstitialAdManager`; `showInterstitial()` + TODO markers for rewarded |
| `ui/views/game/gameScreen/GameScreen.kt` | Exit dialog confirm button calls `viewModel.showInterstitial(...)` |

---

## Related

- [game-deal-flow.md](game-deal-flow.md) — dare and sticky-dare skip flow where the rewarded ad will be wired
