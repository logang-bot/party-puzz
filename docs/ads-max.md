# Ads — AppLovin MAX mediation (`ads/max` branch)

This branch binds `MaxProvider` behind the provider-neutral `AdsProvider` seam. Read [ads.md](ads.md)
first for the seam, the branch workflow and the purchase. This file covers the MAX wiring, the
dashboards, and the keys it needs.

**Why mediation.** Meta Audience Network is bidding-only, so a direct integration (`ads/meta`) may
never fill. MAX runs a real-time auction for every request: AppLovin's own demand and every
enabled network (Meta today, Pangle later) bid, and the highest bid is served. Adding a network
means a Gradle adapter plus ids pasted into the MAX dashboard. **No app code changes.**

Formats are the same as `ads/meta`: **banners** on five screens, a **rewarded** ad for the
premium-pack unlock, and an **interstitial when a game ends**.

> ⚠️ **The keys are placeholders.** Until `MaxKeys` is filled in (see [Keys](#keys-to-import)),
> SDK init fails quietly and nothing shows. Nothing crashes.

---

## What this branch changes vs `main`

| File | Change |
|---|---|
| `ui/common/ads/AdsModule.kt` | Binds `MaxProvider` instead of `NoAdsProvider` |
| `ui/common/ads/max/` | The provider package (below). Only exists on this branch |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | `applovin-sdk` 13.7.0 and `facebook-adapter` 6.22.0.1, plus `USE_TEST_ADS` per build type (`true` for debug/staging) |
| `AndroidManifest.xml` + `res/xml/network_security_config.xml` | Cleartext to `127.0.0.1` only. Meta caches video behind a local proxy |
| `proguard-rules.pro` | Keep rules for AppLovin and Meta (minify is off today, so these are insurance) |
| `src/debug/…/ads/MediationDebuggerActivity.kt` + debug manifest | Opens the Mediation Debugger from adb |

The merged manifest now holds `com.google.android.gms.permission.AD_ID`, which comes from the SDK.
Before releasing from this branch, set Play Console → **Advertising ID** to **Yes** (see
[release.md](release.md)).

## Wiring

```
PartyPuzlApplication.onCreate()
  └─ adsProvider.initialize()   → AppLovinSdk.initialize(SDK_KEY, test GAIDs when USE_TEST_ADS)
                                    └─ on init: MaxInterstitialManager.loadAd()
Screen AdBanner(X)              → MaxBanner(MaxAdView, 50 dp, adaptive off), destroyed on dispose
GameConfigScreen rewarded       → MaxRewardedHandle (shared MaxRewardedAd, reward on onUserRewarded)
GameScreen "exit game" → Yes    → onGameSessionEnded(activity) → MaxInterstitialManager.showIfReady
```

| File | Role |
|---|---|
| `max/MaxProvider.kt` | Implements `AdsProvider`. `bannerHeight = 50.dp` |
| `max/MaxKeys.kt` | SDK key, ad unit ids per `AdPlacement`, and test-device advertising IDs |
| `max/MaxBanner.kt` | Compose wrapper around `MaxAdView` |
| `max/MaxInterstitialManager.kt` | Hilt `@Singleton`. Preloads, shows at game end, reloads on hide or failure with exponential backoff (`MaxRetryDelay`), and skips everything while `isAdFree` |
| `max/MaxRewardedHandle.kt` | `RewardedAdHandle`. MAX shares one `MaxRewardedAd` per unit, so dispose only detaches the listener |

---

## 1. AppLovin dashboard (dash.applovin.com)

1. Sign up and pick **Monetize**. Fill in **Account → Payments** (tax form + payout method).
2. **Account → Keys**: copy the **SDK Key** into `MaxKeys.SDK_KEY`.
3. **MAX → Manage → Ad Units → Create Ad Unit**, Android, package `com.restrusher.partypuzl`:
   5 × **Banner** (one per banner `AdPlacement`), 1 × **Interstitial** (game end), and
   1 × **Rewarded** (pack unlock). Paste each Ad Unit ID into `MaxKeys`.
4. Each ad unit → **Networks** → enable **Meta Audience Network** (bidding), and paste the Meta
   placement id for that unit (step 2.3 below).
5. **MAX → Manage → Test Mode**: add your device's advertising ID (Settings → Google → Ads), and
   pick a network to force. Also add that ID to `MaxKeys.TEST_DEVICE_ADVERTISING_IDS`.
6. Mark the app as **not child-directed**, with a mature audience.

## 2. Meta Monetization Manager (business.facebook.com → Monetization Manager)

1. Create or pick a **Business Portfolio**, then **Create Property** with the Android app's Play
   Store URL. Meta reviews the property. Real ads need the app live on Google Play; test ads
   don't.
2. Choose **AppLovin MAX** as the bidding/mediation partner.
3. **Placements**: Banner × 5, Interstitial × 1, Rewarded × 1. Each id (`APPID_PLACEMENTID`) goes
   into the matching MAX ad unit (step 1.4). **None of them go in app code.**
4. Fill in payout and tax details. Add your device under **Integration → Testing**.
5. Read the Audience Network publisher policy on alcohol content before submitting.

## 3. app-ads.txt

Publish `app-ads.txt` at the root of the developer website set on the Play Store listing. It holds
AppLovin's line (AppLovin dashboard → Account → app-ads.txt) and Meta's line (Monetization Manager).
Without it, many bidders won't bid.

## 4. Later: TikTok demand through Pangle

TikTok for Business is for *buying* ads, not showing them. The publisher side is **Pangle**
(pangleglobal.com): create the app and its placements, add
`com.applovin.mediation:bytedance-adapter` and the Pangle maven repo
(`https://artifact.bytedance.com/repository/pangle`) in `settings.gradle.kts`, then enable Pangle in
each MAX ad unit with its App ID and placement ids. Add Pangle's line to app-ads.txt too.

## Consent (open before release)

MAX's built-in Terms & Privacy flow relies on Google UMP, which is configured in AdMob, and the
AdMob account is banned. If you distribute in the EEA/UK, you need another IAB TCF CMP
(Usercentrics, Didomi, consentmanager…), or you leave those countries out of distribution. The
privacy policy also needs AppLovin's and Meta's disclosures, and the Play **Data safety** form needs
updating.

---

## Keys to import

- [ ] `MaxKeys.SDK_KEY`
- [ ] `MaxKeys.GAME_END_INTERSTITIAL`
- [ ] Five banner ad unit ids and `PACK_UNLOCK_REWARDED` in `MaxKeys`
- [ ] Your test devices in `MaxKeys.TEST_DEVICE_ADVERTISING_IDS` (debug/staging only)
- [ ] A Meta placement id in every MAX ad unit (dashboard only)

## Checks

- [ ] `adb shell am start -n com.restrusher.partypuzl/.ads.MediationDebuggerActivity` on a debug
      build: AppLovin and Meta show as integrated and initialized
- [ ] With Test Mode forcing Meta: banner on Home, Parties, Party detail, Settings and Game config
- [ ] "Exit game" → Yes shows the interstitial. The app stays masked until it closes, then
      returns to where it was
- [ ] The rewarded ad unlocks a pack (once `PACK_UNLOCK_COMING_SOON` is gone, see [ads.md](ads.md))
- [ ] With the app purchased (`isAdFree`): no banners, no gap, no interstitial
