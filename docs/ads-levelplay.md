# Ads — Unity LevelPlay mediation (`ads/levelplay` branch)

This branch binds `LevelPlayProvider` behind the provider-neutral `AdsProvider` seam. Read
[ads.md](ads.md) first for the seam, the branch workflow and the purchase.

**Why LevelPlay.** LevelPlay is ironSource's mediation platform, renamed after the Unity merger. It
runs an auction for every request between **ironSource Ads** and **Unity Ads**. AdMob is banned
and AppLovin is closed to new publishers (see `ads/max`), which leaves this branch as the working
route.

**Meta is left out of this iteration.** LevelPlay is one of Meta's approved bidding partners, so it
can be added later (see [Adding Meta later](#adding-meta-later)) without touching app code beyond
Gradle and the manifest.

Formats: **banners** on five screens, a **rewarded** ad for the premium-pack unlock, and an
**interstitial when a game ends**.

> The app key and all 7 ad unit ids are filled in (`LevelPlayKeys`). Real ads still depend on the
> app passing LevelPlay's review.

---

## What this branch changes vs `main`

| File | Change |
|---|---|
| `ui/common/ads/AdsModule.kt` | Binds `LevelPlayProvider` instead of `NoAdsProvider` |
| `ui/common/ads/levelplay/` | The provider package (below). Only exists on this branch |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | `mediation-sdk` 9.7.0, `unityads-adapter` 5.15.0 + `unity-ads` 4.21.0, plus `USE_TEST_ADS` per build type (`true` for debug/staging) |
| `proguard-rules.pro` | Keep rules for LevelPlay (minify is off today, so these are insurance) |
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
| `levelplay/LevelPlayBannerLogger.kt` | Logs each banner result under the `LevelPlayBanner` tag, e.g. `failed <unit>: 509 Mediation No fill` |
| `levelplay/LevelPlayInterstitialManager.kt` | Hilt `@Singleton`. Preloads, shows at game end, reloads on close or failure with exponential backoff (`LevelPlayRetryDelay`), and skips everything while `isAdFree` |
| `levelplay/LevelPlayRewardedHandle.kt` | `RewardedAdHandle` around `LevelPlayRewardedAd` |

---

## Dashboard setup (done 2026-11-10)

Dashboard menu names change often. If one doesn't match, look for the same idea nearby.

### LevelPlay (ironSource platform)
- App `PartyPuzl` (Android, live app), category Casual, **COPPA: No**. Publisher ID `699667`.
- 7 ad units, ids in `LevelPlayKeys`: 5 × Banner (refresh 30 s), `game_end_interstitial` (pacing:
  max 1 per 3 min) and `pack_unlock_rewarded` (reward `pack` × 1, no capping or pacing).
- **SDK Networks → Unity Ads (Bidding)**: API key and Organization Core ID saved. Auto-setup is
  **off**, because it isn't available for accounts on the new Unity dashboard. Instances use Game ID
  `800079402`:
  `levelplay_banner` → the 5 banners, `levelplay_interstitial` → `game_end_interstitial`,
  `levelplay_rewarded` → `pack_unlock_rewarded`. Every ad unit shows **2** networks.
- **Test devices**: "Second Redmi Note 9 Pro" `9b5e5334-62ba-4c62-9049-12b8d1b53a7c`, the Google
  advertising ID the SDK actually reads, forced to UnityAds Bidding. Xiaomi phones also show a
  separate MIUI ad ID in Privacy settings, which is **not** the one to register. Read the right one
  from logcat: `advertisingId=` in the `LevelPlaySDK` lines. The older "My Redmi Note 9 Pro" entry
  is stale and can be deleted.

### Unity Ads (Unity dashboard → Monetization)
- Project `PartyPuzl`, Android Store ID `com.restrusher.partypuzl`, general audience (not
  child-directed), Game ID `800079402`.
- Placements `levelplay_banner` (refresh 30 s), `levelplay_interstitial` and `levelplay_rewarded`.

### app-ads.txt
Live at **https://logang-bot.github.io/app-ads.txt**, from the repo `logang-bot/logang-bot.github.io`
(GitHub Pages, `main` / root). That repo sits next to this one locally, and also serves a small
landing page `index.html`. The file holds:
- `OWNERDOMAIN=logang-bot.github.io`
- `ironsrc.com, 699667, DIRECT`
- Unity's list, including `unity.com, 114967912, DIRECT, 96cabb5fbdde37a7`, copied from **Unity →
  Monetization → Settings → Organization → App-ads.txt → Show full list**
- the ironSource reseller list from
  https://docs.unity.com/en-us/grow/programmatic/ironsource-exchange/app-ads-txt, without the lines
  already in Unity's list

Both lists change over time. To refresh, re-copy them, rebuild the file and push to that repo.
Unity Ads **won't serve banners at all without a verified app-ads.txt**.

---

## Verified so far (2026-11-10, Redmi Note 9 Pro, debug build)

- LevelPlay init succeeds. The Test Suite shows ironSource 9.7.0 and Unity Ads 4.21.0 (adapter
  5.15.0) integrated, each with Rewarded, Interstitial and Banner.
- Test Suite → `home_banner` in **Test** mode: a Unity Ads test banner and an ironSource "Demo App"
  test banner both loaded and displayed.
- In the app, `game_end_interstitial` loads (Unity Ads) on every launch.
- In the app, every banner returns **`509 Mediation No fill`** with live demand. That's expected
  until app-ads.txt is verified and the app has some traffic.

---

## Open items

**Finish testing**
- [ ] Phone recognized as a test device. In the app (not the Test Suite), the home banner should
      fill with a Unity Ads test banner. While it isn't recognized the phone receives **real ads**:
      don't tap them. If still unrecognized, click **Reset** on the test device row, and check that
      Settings → Google → Ads shows the same ID with ads personalization not deleted.
- [ ] Test Suite, Test mode: load and show `game_end_interstitial` (`4t9hlie2nennlxmc`) and
      `pack_unlock_rewarded` (`461k6cho7k1iadso`)
- [ ] Banner on Home, Parties, Party detail, Settings and Game config, with no layout jump once it
      loads
- [ ] "Exit game" → Yes shows the interstitial. The app stays masked until it closes, and pacing
      holds it to 1 per 3 min
- [ ] With the app purchased (`isAdFree`): no banners, no gap, no interstitial

**app-ads.txt**
- [ ] Play Console → Store presence → Store settings → **Website** = `https://logang-bot.github.io`
- [ ] Unity → Monetization → Settings → Organization → Organization info → **Developer Website** =
      `https://logang-bot.github.io`
- [ ] A few days later: Unity's Organization page and LevelPlay report app-ads.txt as verified
- [ ] Monthly: compare both seller lists against the live file and update it

**Before releasing from this branch**
- [ ] Privacy policy (`partypuzl-legal`) adds ironSource/Unity Ads disclosures: advertising ID,
      device data, ad personalization
- [ ] Play Console **Data safety** updated for the ad SDKs
- [ ] Play Console **Ads**: "contains ads" = Yes. **Advertising ID** = Yes (the merged manifest
      has `AD_ID`)
- [ ] Consent: if distributing in the EEA/UK, add an IAB TCF consent tool (CMP); otherwise
      exclude those countries
- [ ] Re-enable the pack unlock: remove `FeatureFlags.PACK_UNLOCK_COMING_SOON` (steps in
      [ads.md](ads.md)). The `remove_ads` product must exist in Play Console first
- [ ] Release build smoke test: no test tools (`USE_TEST_ADS = false`), and ads appear for a
      non-test device

---

## Adding Meta later

The Meta property already exists in Monetization Manager (Banner, Interstitial and Video with prize
placements).
1. Add `com.unity3d.ads-mediation:facebook-adapter` to Gradle.
2. Restore `res/xml/network_security_config.xml` (cleartext to `127.0.0.1` only, because Meta caches
   video behind a local proxy) and the manifest attribute, plus the `com.facebook.ads` keep rules.
   `ads/max` has all three to copy.
3. In LevelPlay **SDK Networks**, add Meta with those placement ids.
4. In Monetization Manager, set the property's **Mediation platform** to **Unity LevelPlay**.
5. Add Meta's line to app-ads.txt.
