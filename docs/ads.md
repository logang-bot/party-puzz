# Ads

The app does not depend on any one ad network. Screens and lifecycle code talk to a provider-neutral
interface, `AdsProvider`. Which network answers is decided by **one Hilt binding**, and each network
lives on its own branch:

| Branch | Provider bound in `AdsModule` | Ads | Provider doc |
|---|---|---|---|
| `main` | `NoAdsProvider` | **None.** No ad SDK is in the build | this file |
| `ads/admob` | `AdMobProvider` | Banners, App Open, rewarded | `docs/ads-admob.md` (on that branch) |
| `ads/meta` | `MetaProvider` | Banners, rewarded, interstitial after a game | `docs/ads-meta.md` (on that branch) |

`main` ships without ads because the AdMob account is suspended. The billing purchase still works on
every branch (see [Purchase app](#purchase-app--in-app-purchase)).

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

## The pack unlock is switched off

⚠️ **Both** routes into unlocking a premium pack, "watch a short ad" *and* the one-time purchase,
currently just raise a **"Coming soon!"** toast. They are gated by
`FeatureFlags.PACK_UNLOCK_COMING_SOON`. This is deliberate and **temporary**.

**Why.** Neither route can earn yet:
- `main` has no ad provider at all.
- `ads/admob` still points the rewarded placement at Google's *test* unit.
- `ads/meta` has placeholder placement ids.
- The `remove_ads` product is not set up in Play Console.

**What is stubbed.** Only the two click handlers in `GameConfigScreen`. Everything underneath is still
wired: `RewardedAdHandle`, `BillingManager`, `SessionUnlocksSource`, `onRewardEarned` and the sheet.
The screen also skips `rememberRewardedAd` while the flag is on, so no ad requests are made.

**To undo** (on the branch you ship):

1. Give `PACK_UNLOCK_REWARDED` a real unit / placement id (see that branch's ads doc).
2. Create and activate `remove_ads` in Play Console (setup step 4 in [release.md](release.md)).
3. Delete `PACK_UNLOCK_COMING_SOON` from `ui/common/FeatureFlags.kt` and the `comingSoon` branches
   in `GameConfigScreen`.
4. Delete the `coming_soon` string from both `strings.xml` files.
5. Delete this section, and the note in [question-packs.md](question-packs.md).

On `main` the "Watch a short ad" row can never become ready even with the flag off, because
`NoAdsProvider` never loads anything.

---

## Purchase app — In-App Purchase

A single one-time purchase. Settings shows it as **"Purchase App"** ("One-time purchase · All
packs, no ads"). It used to be called "Remove Ads". It was renamed because it also unlocks every
premium question pack, which matters on `main` where there are no ads to remove.

Only the user-facing text changed. The code names stay as they were:

| Thing | Name | Why it was not renamed |
|---|---|---|
| Play product id | `remove_ads` (`BillingManager.REMOVE_ADS_PRODUCT_ID`) | Must match Play Console exactly |
| Owned flag | `UserPreferencesRepository.isAdFree` | Persisted in DataStore and mirrored onto `question_packs` rows |
| Row / action | `PurchaseAppRow` / `SettingsViewModel.purchaseApp` | |
| Strings | `purchase_app`, `purchase_app_subtitle`, `app_purchased`, `app_purchased_subtitle` | |

Only `PackTier.PREMIUM` is gated. Official packs are free. Custom packs are always unlocked. See
[question-packs.md](question-packs.md) and [custom-packs.md](custom-packs.md).

> **TODO:** Play Console → Monetize → Products → In-app products → Create product. Set the id to
> exactly `remove_ads`, type **One-time**, set the price, and activate it. Tracked as setup step 4 in
> [release.md](release.md). While it is missing, `launchPurchaseFlow` returns `false` and
> `GameConfigScreen` shows the `unlock_purchase_unavailable` snackbar.

### Purchase flow

1. `BillingManager.connect()` runs in `PartyPuzlApplication.onCreate()`.
2. Once connected, it queries existing purchases and the product details.
3. If `remove_ads` is already owned, `UserPreferencesRepository.setAdFree(true)` persists that.
4. Settings → Purchases → **Purchase App** calls `BillingManager.launchPurchaseFlow(activity)`.
5. On success the purchase is acknowledged, and `isAdFree` flips to `true` app-wide.

### How `isAdFree` propagates

| Layer | Mechanism |
|---|---|
| DataStore | `UserPreferencesRepository.isAdFree: Flow<Boolean>` is the source of truth |
| Banners and padding | `LocalIsAdFree`, provided in `MainActivity`. `AdBanner` and `adBannerReservedHeight` check it |
| Full-screen ads | Each provider collects `isAdFree` and skips loads and shows |
| Settings | `PurchaseAppRow` switches to "App Purchased" |
| Question packs | `GameConfigViewModel` treats it as "all premium unlocked" |
