# Ads

The app does not depend on any one ad network. Screens and lifecycle code talk to a provider-neutral
interface, `AdsProvider`. Which network answers is decided by **one Hilt binding**, and each network
lives on its own branch:

| Branch | Provider bound in `AdsModule` | Ads | Provider doc |
|---|---|---|---|
| `main` | `NoAdsProvider` | **None.** No ad SDK is in the build | this file |
| `ads/admob` | `AdMobProvider` | Banners, App Open, rewarded | `docs/ads-admob.md` (on that branch) |
| `ads/meta` | `MetaProvider` | Banners, rewarded, interstitial after a game | `docs/ads-meta.md` (on that branch) |

`main` ships without ads because there is no working AdMob account. The billing code is wired on
every branch, but it can't be bought yet: the pack unlock is switched off (see
[below](#the-pack-unlock-is-switched-off)).

---

## Branch workflow

All feature work happens on `main`. To bring it into an ad branch:

```bash
git checkout ads/admob && git merge main     # same for ads/meta
```

The merge stays clean as long as `main` leaves the files each branch owns alone:

| Owned by the ad branch | Why it differs |
|---|---|
| `ui/common/ads/AdsModule.kt` | the one-line binding (`NoAdsProvider` becomes `AdMobProvider` / `MetaProvider`) |
| `ui/common/ads/admob/` or `ui/common/ads/meta/` | the provider package. Only the branch has it |
| `app/build.gradle.kts`, `gradle/libs.versions.toml` | the SDK dependency, `USE_TEST_ADS`, and the AdMob App ID placeholder |
| `AndroidManifest.xml` (+ `res/xml/` on Meta) | the AdMob App ID meta-data / the Meta network security config |
| `docs/ads-*.md`, ad lines in `docs/release.md` and `docs/builds.md` | provider-specific setup |

If `main` has to change one of these files, expect a small conflict on the next merge, and keep the
branch's version of the ad lines.

**To ship with a provider**, release from that branch. Swapping providers means releasing from the
other branch. No code moves.

---

## The seam (`ui/common/ads/`)

These files are identical on every branch.

| File | Role |
|---|---|
| `AdPlacement.kt` | Enum of every ad spot: five banners plus `PACK_UNLOCK_REWARDED`. Screens name a placement, never a unit id |
| `AdsProvider.kt` | The interface a provider implements (below) |
| `RewardedAdHandle.kt` | `isReady`, `show(activity, onRewarded)` and `reload()`. `show` consumes the ad |
| `AdsLocals.kt` | `LocalIsAdFree`, `LocalAdsProvider`, and the call-site helpers `AdBanner`, `adBannerReservedHeight`, `rememberRewardedAd` and `rememberGameSessionExit` |
| `RewardedAdCard.kt` | Styled standalone card for a `RewardedAdHandle`. Currently unused |
| `none/NoAdsProvider.kt` | `main`'s provider. It renders nothing, is never ready, and does nothing on any lifecycle call |
| `AdsModule.kt` | `@Provides AdsProvider` and binds the branch's provider |

### `AdsProvider`

| Member | Called from | Purpose |
|---|---|---|
| `initialize()` | `PartyPuzlApplication.onCreate()` | SDK init and the first preloads |
| `onActivityStarted(activity)` / `onActivityStopped()` | `MainActivity.onStart` / `onStop` | Launch-time ads (AdMob App Open) |
| `onGameSessionEnded(activity)` | `GameScreen`, when the player confirms leaving the game | Between-sessions slot (Meta interstitial) |
| `isFullScreenAdVisible` | `MainActivity.setContent` | Compose state. While true, a background-coloured `Box` masks the app so nothing shifts behind the ad |
| `bannerHeight` | `adBannerReservedHeight()` | Bottom padding screens reserve for the pinned banner. 0 dp on `main` |
| `Banner(placement, modifier)` | `AdBanner(...)` | The banner view |
| `rememberRewarded(placement)` | `rememberRewardedAd(...)` | A remembered `RewardedAdHandle` |

`AdBanner` and `adBannerReservedHeight` return early or 0 dp when `LocalIsAdFree` is true, so no
provider has to check that itself. Full-screen ads read `UserPreferencesRepository.isAdFree`
inside the provider.

### Where placements appear

| Placement | Screen | Position |
|---|---|---|
| `HOME_BANNER` | `HomeScreen` | Pinned bottom. The scroll content pads by `adBannerReservedHeight()` |
| `PARTIES_BANNER` | `PartiesScreen` | Pinned bottom. `LazyColumn` content padding |
| `PARTY_DETAIL_BANNER` | `PartyDetailScreen` | Pinned bottom |
| `SETTINGS_BANNER` | `SettingsScreen` | Pinned bottom |
| `GAME_CONFIG_BANNER` | `GameConfigScreen` | Inline, between the mode header and the players |
| `PACK_UNLOCK_REWARDED` | `GameConfigScreen` → `UnlockChoiceBottomSheet` | "Watch a short ad" unlocks a premium pack for the session |

### Adding a new provider

1. Branch from `main` as `ads/<name>`.
2. Add `ui/common/ads/<name>/` with a `@Singleton class <Name>Provider @Inject constructor(...) : AdsProvider`.
3. Point `AdsModule.provideAdsProvider` at it.
4. Add the SDK, any manifest entries, and a `docs/ads-<name>.md` listing every key it needs.

---

## Premium — the one-time purchase

One one-time product unlocks **premium**: every `PackTier.PREMIUM` pack and unlimited custom packs
(free users get 1 custom pack of up to 5 entries — see [custom-packs.md](custom-packs.md)). On
builds with an ad provider it also removes ads. Copy on `main` never promises ad removal, because
`main` shows none and Play treats that as a misleading claim.

The code still uses some older names:

| Thing | Name | Why it was not renamed |
|---|---|---|
| Play product id | `remove_ads` (`PREMIUM_PRODUCT_ID` in `data/billing/PremiumProduct.kt`) | Already created in Play Console; ids can never be renamed or reused |
| Owned flag | `UserPreferencesRepository.isAdFree` / `LocalIsAdFree` | Renaming it would conflict with every `ads/*` branch |

### Entry points

Both open Play's purchase sheet through `PremiumPurchaseViewModel.purchase(activity)`:

1. **Setup screen**: tapping a locked premium pack opens `UnlockChoiceBottomSheet`. Its "Watch a
   short ad" row only appears when `AdsProvider.supportsRewardedAds` is true, so on `main`
   (`NoAdsProvider`) the sheet offers the purchase alone.
2. **Settings → Purchases → Purchase App** (`PurchaseAppRow`). It shows "App Purchased" once owned.

The custom-pack manager and editor add a third and fourth: `PremiumUpsellSheet` opens when a free
user hits the pack or entry cap. Every purchase button shows Play's localised price
(`PremiumBilling.formattedPrice`) once the product has loaded.

### Billing pieces (`data/billing/`)

| File | Role |
|---|---|
| `PremiumBilling` | Interface the app depends on, bound in `di/BillingModule` |
| `BillingManager` | Play `BillingClient`: connection, product query, purchase flow, acknowledgement |
| `PurchaseEntitlementSync` | Turns Play's purchase lists into `isAdFree` (unit-tested) |
| `PurchaseOutcome` | `Purchased` / `Pending` / `Cancelled` / `Failed` / `Unavailable` |
| `PremiumProduct.kt` | The product id and the `Purchase.isPremiumOwned` / `isPremiumPending` checks |

### Purchase flow

1. `PartyPuzlApplication.onCreate()` calls `connect()`. Once connected, it loads the product
   details (and price) and runs `refreshEntitlement()`.
2. `MainActivity.onStart()` calls `refreshEntitlement()` again on every return to the app.
3. `refreshEntitlement()` reads Play's **owned** purchases and sets `isAdFree` to whether premium
   is among them. A refunded or revoked purchase disappears from that list, so this **relocks**.
   Unacknowledged purchases are acknowledged here too, which retries any that failed before.
4. A purchase reports back through `onPurchasesUpdated`. Completed → `isAdFree = true`, acknowledged,
   `Purchased`. Pending (e.g. cash payment) → nothing granted yet, `Pending`; it unlocks on the
   next refresh after it clears. `ITEM_ALREADY_OWNED` triggers a refresh.
5. `MainActivity` collects `PremiumBilling.outcomes` and shows a toast for every outcome except
   a cancel. `Unavailable` (product never loaded: no Play connection, or a build not installed from
   Play) uses `unlock_purchase_unavailable`.

### How `isAdFree` propagates

| Layer | Mechanism |
|---|---|
| DataStore | `UserPreferencesRepository.isAdFree: Flow<Boolean>` is the source of truth |
| Question packs | `GameConfigViewModel` treats it as "all premium unlocked", and mirrors it both ways onto the premium `question_packs` rows with `setPremiumUnlocked`. Relocking also switches those packs off, because the deal only reads `isEnabled` |
| Custom packs | `ObserveCustomPackQuotaUseCase` lifts the pack and entry caps |
| Settings | `PurchaseAppRow` switches to "App Purchased" |
| Banners and full-screen ads (ad branches) | `LocalIsAdFree` and each provider skip ads |

A reinstall or new phone starts with `isAdFree = false`; the first refresh finds the owned product
and sets it back. Play Console setup is in [release.md](release.md), setup step 4.

---

## Known issue: App Open ad shows on every return to the app (`ads/admob`)

**User feedback:** "too many ads". The full-screen ad came back every time people left the app and
returned, including right after taking a photo.

**Cause.** `MainActivity.onStart()` calls `adsProvider.onActivityStarted(this)`, and
`AdMobProvider` forwards that to `AdMobAppOpenManager.showWhenReady()`. `onStart` runs on **every**
return to the foreground, not just launch:

- coming back from the home screen or the recents list
- coming back from the **system camera**, on branches cut before the in-app camera. There the photo
  features hand off to the camera app, so `MainActivity` stops and starts again. On `main` the camera
  is in-app ([in-app-camera.md](in-app-camera.md)) and taking a photo no longer triggers `onStart`
- the activity being recreated, for example on a language change in Settings (`applyLanguage`)

The manager's own doc says it all: "shows it on every launch / return to the foreground". After
each dismissal it immediately preloads the next ad, so one is nearly always ready.

`ads/meta` is not affected. Its `onActivityStarted` is a no-op, and its interstitial only shows
when a game ends.

**Fix (planned for a release after 1.0, on `ads/admob`).** Show the App Open ad only on a **cold
start**, meaning once per process:

1. Add a `private var hasShownThisProcess = false` to `AdMobAppOpenManager`. `showWhenReady()`
   returns early when it is `true`, and `showAd()` sets it once the ad actually shows. The manager is
   a `@Singleton`, so the flag resets only when the process dies, which is exactly a fresh open.
2. Keep the preload, so the cold-start ad is ready on the next launch.
3. Optional: if a warm return after a long absence (for example 4 h or more) should also count as
   an open, compare against a timestamp of the last `onStop`. Never do this for returns from the
   camera.
4. Update the manager's KDoc and the `AdsProvider` table above ("Launch-time ads").

**Verify:** open the app (ad shows), take a party photo (no ad), go home and come back (no ad),
change the language (no ad), then swipe the app away and open it again (ad shows).

The fix is on the seam's provider side only, so `main` and `ads/meta` don't change.
