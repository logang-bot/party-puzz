# Ads — Meta Audience Network (`ads/meta` branch)

This branch binds `MetaProvider` behind the provider-neutral `AdsProvider` seam. Read
[ads.md](ads.md) first for the seam, the branch workflow and the purchase. This file covers only the
Meta wiring and the keys it needs.

Formats: **banners** on five screens, a **rewarded** video for the premium-pack unlock, and an
**interstitial when a game ends**. Meta has no App Open format. Its policy also discourages
interstitials on launch, so the full-screen slot sits at the natural break instead: the player
confirms "exit game" on `GameScreen`.

> ⚠️ **Placement ids are placeholders.** Until they are replaced (see [Keys](#keys-to-import)),
> every request fails quietly. Nothing crashes, but nothing shows either.

> ⚠️ **Check fill before relying on this.** Audience Network is **bidding-only**. Monetization
> Manager must show this app's property as eligible to serve through a direct, single-network SDK
> integration. If it asks for a bidding / mediation partner instead (AdMob, AppLovin MAX, LevelPlay),
> this branch will not fill on its own.

---

## What this branch changes vs `main`

| File | Change |
|---|---|
| `ui/common/ads/AdsModule.kt` | Binds `MetaProvider` instead of `NoAdsProvider` |
| `ui/common/ads/meta/` | The provider package (below). Only exists on this branch |
| `gradle/libs.versions.toml` | `audienceNetworkSdk = "6.22.0"` and the `audience-network-sdk` library |
| `app/build.gradle.kts` | `implementation(libs.audience.network.sdk)`, plus `USE_TEST_ADS` per build type (`true` for debug/staging) |
| `AndroidManifest.xml` | `android:networkSecurityConfig="@xml/network_security_config"` |
| `res/xml/network_security_config.xml` | Allows cleartext to `127.0.0.1` only. The SDK caches video ads behind a local proxy |
| `proguard-rules.pro` | Keep rules for `com.facebook.ads` (minify is off today, so this is insurance) |

Nothing else differs, so `git merge main` stays clean. One exception: if `main` ever adds its own
network security config, merge the `127.0.0.1` domain into it.

---

## Wiring

```
PartyPuzlApplication.onCreate()
  └─ adsProvider.initialize()        → AdSettings.setTestMode(USE_TEST_ADS)
                                       AudienceNetworkAds.initialize()
                                       MetaInterstitialManager.loadAd()   (preload the game-end ad)

MainActivity.onStart / onStop        → no-ops (no launch ad)
MainActivity.setContent              → masks the app while the interstitial is up

Screen  AdBanner(AdPlacement.X)      → MetaProvider.Banner → AdView(placement, BANNER_HEIGHT_50), destroyed on dispose
GameConfigScreen rememberRewardedAd  → MetaProvider.rememberRewarded → MetaRewardedAd (RewardedVideoAd), destroyed on dispose
GameScreen "exit game" → Yes         → rememberGameSessionExit → onGameSessionEnded → MetaInterstitialManager.showIfReady()
                                       then navigates back as before
```

| File | Role |
|---|---|
| `meta/MetaProvider.kt` | Implements `AdsProvider`. `bannerHeight = 50.dp` |
| `meta/MetaInterstitialManager.kt` | Hilt `@Singleton`. Preloads one interstitial, shows it on game end if loaded, and reloads after it is dismissed. If nothing is ready, the exit simply proceeds and a load starts. Skips everything while `isAdFree` |
| `meta/MetaRewardedAd.kt` | `RewardedAdHandle` around `RewardedVideoAd`. The reward is granted on `onRewardedVideoCompleted`. `show()` consumes the ad, and `GameConfigScreen` calls `reload()` in the reward callback |
| `meta/MetaPlacementIds.kt` | Maps `AdPlacement` to a placement id, plus `GAME_END_INTERSTITIAL` |

---

## Keys to import

Setup lives in Meta Business Suite → **Monetization Manager** (business.facebook.com/pub).

1. Create (or reuse) a **Meta app** at developers.facebook.com, then add the **Audience Network**
   product to it.
2. In Monetization Manager, create a **property** for Party Puzz and add the Android platform with
   package `com.restrusher.partypuzl` and the Play Store URL.
3. Create these **placements**. Each id looks like `<APP_ID>_<PLACEMENT_ID>`:

| Placement | Format in Monetization Manager | Where it goes |
|---|---|---|
| Home banner | Banner | `MetaPlacementIds` → `HOME_BANNER` |
| Parties banner | Banner | `PARTIES_BANNER` |
| Party detail banner | Banner | `PARTY_DETAIL_BANNER` |
| Settings banner | Banner | `SETTINGS_BANNER` |
| Game setup banner | Banner | `GAME_CONFIG_BANNER` |
| Pack unlock | Rewarded video | `PACK_UNLOCK_REWARDED` |
| Game end | Interstitial | `GAME_END_INTERSTITIAL` |

4. **`app-ads.txt`**: add the line Monetization Manager shows (under *Integration → app-ads.txt*) to the
   developer website listed in Play Console.
5. **Payout + tax info** in Monetization Manager, or the property never goes live.

No App ID goes in the manifest. Meta reads it from the placement id.

### Test mode

Meta has no public test ids. Test builds use the **real** placement ids with test mode on:

- `debug` and `staging`: `USE_TEST_ADS = true`, so `AdSettings.setTestMode(true)` serves Meta's test
  creatives and impressions don't count.
- `release`: test mode is off and real ads serve.
- On a physical device, you may also need to register it under Monetization Manager →
  *Integration → Testing*. To pick a specific test creative (e.g. video), set the test ad type there.

Never ship a build with test mode on, and never click real ads on your own device.

---

## Release checks (this branch)

- [ ] All seven placeholders in `MetaPlacementIds` are replaced
- [ ] Staging shows Meta test banners on Home, Parties, Party detail, Settings and Game setup
- [ ] No ad on app launch
- [ ] Exiting a game shows the interstitial, then returns to the setup screen. With no ad loaded, the exit is instant
- [ ] After purchasing the app: no banners, no interstitial, and no bottom gap
- [ ] The property is approved / live in Monetization Manager, and `app-ads.txt` verifies
- [ ] The Rewarded placement is real before `FeatureFlags.PACK_UNLOCK_COMING_SOON` is removed
- [ ] Play Console: *Ads declaration* says the app contains ads, and *Data safety* includes Meta Audience Network's disclosures (device identifiers / advertising ID)
