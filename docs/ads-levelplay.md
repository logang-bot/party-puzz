# Ads — Unity LevelPlay mediation (`ads/levelplay` branch)

This branch binds `LevelPlayProvider` behind the provider-neutral `AdsProvider` seam. Read
[ads.md](ads.md) first for the seam, the branch workflow and the purchase.

**Why LevelPlay.** LevelPlay is ironSource's mediation platform, renamed after the Unity merger. It
runs an auction for every request between **ironSource Ads**, **Unity Ads** and **Meta Audience
Network**. Meta is bidding-only and only serves through approved partners, and LevelPlay is one of
them. AdMob is banned and AppLovin is closed to new publishers (see `ads/max`), which leaves this
branch as the working route.

Formats: **banners** on five screens, a **rewarded** ad for the premium-pack unlock, and an
**interstitial when a game ends**.

> ⚠️ **The keys are placeholders.** Until `LevelPlayKeys` is filled in, init fails quietly and
> nothing shows. Nothing crashes.

---

## What this branch changes vs `main`

| File | Change |
|---|---|
| `ui/common/ads/AdsModule.kt` | Binds `LevelPlayProvider` instead of `NoAdsProvider` |
| `ui/common/ads/levelplay/` | The provider package (below). Only exists on this branch |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | `mediation-sdk` 9.7.0, `facebook-adapter` 5.6.0, `unityads-adapter` 5.15.0 + `unity-ads` 4.21.0, plus `USE_TEST_ADS` per build type (`true` for debug/staging) |
| `AndroidManifest.xml` + `res/xml/network_security_config.xml` | Cleartext to `127.0.0.1` only. Meta caches video behind a local proxy |
| `proguard-rules.pro` | Keep rules for LevelPlay and Meta (minify is off today, so these are insurance) |
| `src/debug/…/ads/LevelPlayTestSuiteActivity.kt` + debug manifest | Opens the LevelPlay Test Suite from adb |

The merged manifest holds `com.google.android.gms.permission.AD_ID`, which comes from the SDKs.
Before releasing from this branch, set Play Console → **Advertising ID** to **Yes** (see
[release.md](release.md)).

The SDK also pulls `adquality-sdk` with a version *range* (`[9.10.0,10.0.0)`), so Gradle resolves it to
the newest matching release at build time (9.11.0 when this was written).

## Wiring

```
PartyPuzlApplication.onCreate()
  └─ adsProvider.initialize()   → LevelPlay.init(APP_KEY); debug/staging also enable the Test Suite
                                    └─ onInitSuccess: isSdkReady = true, preload the interstitial
Screen AdBanner(X)              → LevelPlayBanner: empty 50 dp until isSdkReady, then a 320×50 banner
GameConfigScreen rewarded       → LevelPlayRewardedHandle, loaded once isSdkReady (reward on onAdRewarded)
GameScreen "exit game" → Yes    → onGameSessionEnded(activity) → LevelPlayInterstitialManager.showIfReady
```

LevelPlay ad objects may only be created **after init succeeds**, so banners and the rewarded ad wait
for `isSdkReady`.

| File | Role |
|---|---|
| `levelplay/LevelPlayProvider.kt` | Implements `AdsProvider`, runs init, and exposes `isSdkReady` to the banner and rewarded ad |
| `levelplay/LevelPlayKeys.kt` | App key and ad unit ids per `AdPlacement` |
| `levelplay/LevelPlayBanner.kt` | Compose wrapper around `LevelPlayBannerAdView` |
| `levelplay/LevelPlayInterstitialManager.kt` | Hilt `@Singleton`. Preloads, shows at game end, reloads on close or failure with exponential backoff (`LevelPlayRetryDelay`), and skips everything while `isAdFree` |
| `levelplay/LevelPlayRewardedHandle.kt` | `RewardedAdHandle` around `LevelPlayRewardedAd` |

---

## Dashboard setup

Dashboard menu names change often. If one doesn't match, look for the same idea nearby.

### 1. LevelPlay (Unity dashboard → Grow → LevelPlay)
1. Sign up or sign in, and complete the company, payment and tax details.
2. **Add app**: Android, package `com.restrusher.partypuzl` (or the Play Store URL). Copy the
   **App Key** into `LevelPlayKeys.APP_KEY`.
3. **Ad units**: create 5 × Banner, 1 × Interstitial and 1 × Rewarded. Paste each ad unit id into
   `LevelPlayKeys`.
4. **Networks / SDK networks**: set up **Meta Audience Network** (bidding) with the Meta placement
   ids, and turn on **Unity Ads**. ironSource Ads is on by default.
5. **Test devices**: add your phone's advertising ID (Settings → Google → Ads) for test ads.

### 2. Meta Monetization Manager (https://www.facebook.com/pub/start)
1. On the PartyPuzl property, set **Integration → Mediation platform** to **Unity LevelPlay**. It
   was set to AppLovin MAX.
2. Keep the existing placements: Banner, Interstitial, Video with prize. Each ID goes only into the
   LevelPlay dashboard, never into app code.

### 3. app-ads.txt
Publish `app-ads.txt` at the root of the developer website on the Play listing
(`https://logang-bot.github.io/app-ads.txt` needs a `logang-bot.github.io` repo). It holds the lines
LevelPlay gives you, plus Meta's line from Monetization Manager.

## Consent (open before release)

If you distribute in the EEA/UK, you need an IAB TCF consent tool (CMP). LevelPlay reads the
consent signal it writes. The privacy policy also needs the networks' disclosures, and the Play
**Data safety** form needs updating.

---

## Keys to import

- [ ] `LevelPlayKeys.APP_KEY`
- [ ] `LevelPlayKeys.GAME_END_INTERSTITIAL`
- [ ] Five banner ad unit ids and `PACK_UNLOCK_REWARDED` in `LevelPlayKeys`
- [ ] Meta placement ids in the LevelPlay dashboard's Meta setup (dashboard only)

## Checks

- [ ] Launch a debug build once, then run
      `adb shell am start -n com.restrusher.partypuzl/.ads.LevelPlayTestSuiteActivity`. ironSource,
      Unity Ads and Meta should all show as integrated
- [ ] Banner on Home, Parties, Party detail, Settings and Game config, with no layout jump once it
      loads
- [ ] "Exit game" → Yes shows the interstitial. The app stays masked until it closes
- [ ] The rewarded ad unlocks a pack (once `PACK_UNLOCK_COMING_SOON` is gone, see [ads.md](ads.md))
- [ ] With the app purchased (`isAdFree`): no banners, no gap, no interstitial
