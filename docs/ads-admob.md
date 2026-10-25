# Ads — Google AdMob (`ads/admob` branch)

This branch binds `AdMobProvider` behind the provider-neutral `AdsProvider` seam. Read
[ads.md](ads.md) first for the seam, the branch workflow and the purchase. This file covers only the
AdMob wiring and the keys it needs.

Formats: **banners** on five screens, an **App Open** ad on every launch / return to foreground, and a
**rewarded** ad for the premium-pack unlock.

---

## What this branch changes vs `main`

| File | Change |
|---|---|
| `ui/common/ads/AdsModule.kt` | Binds `AdMobProvider` instead of `NoAdsProvider` |
| `ui/common/ads/admob/` | The provider package (below). Only exists on this branch |
| `gradle/libs.versions.toml` | `playServicesAds = "23.6.0"` and the `play-services-ads` library |
| `app/build.gradle.kts` | `implementation(libs.play.services.ads)`, plus per-build-type `admobAppId` and `USE_TEST_ADS` |
| `AndroidManifest.xml` | `com.google.android.gms.ads.APPLICATION_ID` meta-data → `${admobAppId}` |

Nothing else differs, so `git merge main` stays clean.

---

## Wiring

```
PartyPuzlApplication.onCreate()
  └─ adsProvider.initialize()        → MobileAds.initialize() + AdMobAppOpenManager.loadAd()

MainActivity.onStart()               → adsProvider.onActivityStarted() → appOpenManager.showWhenReady(activity)
MainActivity.onStop()                → adsProvider.onActivityStopped() → appOpenManager.clearPendingActivity()
MainActivity.setContent              → masks the app while adsProvider.isFullScreenAdVisible

Screen  AdBanner(AdPlacement.X)      → AdMobProvider.Banner → AdView(AdSize.BANNER, AdMobUnitIds.of(X))
GameConfigScreen rememberRewardedAd  → AdMobProvider.rememberRewarded → AdMobRewardedAd.reload()
GameScreen exit                      → onGameSessionEnded → no-op on AdMob
```

| File | Role |
|---|---|
| `admob/AdMobProvider.kt` | Implements `AdsProvider`. `bannerHeight = 50.dp` |
| `admob/AdMobAppOpenManager.kt` | Hilt `@Singleton`. Loads the App Open ad and shows it when fresh (< 4 h). Otherwise it stores a `WeakReference<Activity>` and shows the ad as soon as it loads. Skips everything while `isAdFree` |
| `admob/AdMobRewardedAd.kt` | `RewardedAdHandle` around `RewardedAd`. `show()` consumes the ad, and `GameConfigScreen` calls `reload()` in the reward callback |
| `admob/AdMobUnitIds.kt` | Maps `AdPlacement` to a unit id, plus `appOpen`. Uses test ids when `BuildConfig.USE_TEST_ADS` |

When the App Open ad shows, the SDK hides the status bar. The full-screen mask in `MainActivity`
stops the app from shifting visibly behind it.

---

## Keys to import

Everything comes from the AdMob console (apps.admob.com) → your app.

| Key | Where it goes | Status |
|---|---|---|
| **App ID** (`ca-app-pub-XXXX~YYYY`) | `app/build.gradle.kts` → `release { manifestPlaceholders["admobAppId"] = "…" }` | Set (`ca-app-pub-7803968519747509~8418313584`) |
| `HOME_BANNER` unit | `AdMobUnitIds.production` | Set |
| `PARTIES_BANNER` unit | `AdMobUnitIds.production` | Set |
| `PARTY_DETAIL_BANNER` unit | `AdMobUnitIds.production` | Set |
| `SETTINGS_BANNER` unit | `AdMobUnitIds.production` | Set |
| `GAME_CONFIG_BANNER` unit | `AdMobUnitIds.production` | Set |
| App Open unit | `AdMobUnitIds.PRODUCTION_APP_OPEN` | Set |
| **Rewarded unit** | `AdMobUnitIds.production[PACK_UNLOCK_REWARDED]` | **TODO.** Still Google's test unit. Create *Ad units → Add ad unit → Rewarded* |
| `app-ads.txt` | Root of the developer website listed in Play Console | Add the line AdMob shows under *Apps → app-ads.txt* |

Test keys need no action. `debug` and `staging` use Google's sample App ID
(`ca-app-pub-3940256099942544~3347511713`) and the test units in `AdMobUnitIds` (`USE_TEST_ADS = true`).
Never put production ids in a debuggable build, because clicks on your own ads count as invalid traffic.

If the account is reinstated under a **different** publisher id, replace every
`ca-app-pub-7803968519747509` value: the App ID and all unit ids.

---

## Release checks (this branch)

- [ ] Staging shows test banners on Home, Parties, Party detail, Settings and Game setup
- [ ] App Open ad shows on cold start and on return from background
- [ ] After purchasing the app, no banners, no App Open, and no bottom gap
- [ ] AdMob has approved the app (it crawls the Play listing, which can take days)
- [ ] The Rewarded unit is real before `FeatureFlags.PACK_UNLOCK_COMING_SOON` is removed
- [ ] Play Console: *Ads declaration* says the app contains ads, and *Data safety* includes AdMob's disclosures
