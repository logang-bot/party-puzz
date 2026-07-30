package com.restrusher.partypuzl.ui.common

/**
 * Switches for features that are built but deliberately not live yet.
 *
 * Everything here is **temporary by definition**. A flag in this file is a promise to come back
 * and delete it, not a permanent configuration surface — anything meant to stay belongs in
 * `BuildConfig` or user preferences instead.
 */
object FeatureFlags {

    /**
     * Stubs out both routes into unlocking a premium pack — "watch a short ad" and the one-time
     * purchase — so each just says "Coming soon" instead.
     *
     * **Why:** the pack unlock is not ready to earn. `AdUnitIds.Production.PACK_UNLOCK_REWARDED`
     * still points at Google's *test* rewarded unit, so a release build would run the whole flow
     * and make nothing, and the `remove_ads` product is not set up for launch either. Showing a
     * user a paid door that leads nowhere is worse than not showing the door.
     *
     * **To undo, once the real ad unit and product exist:**
     * 1. Create the Rewarded unit in AdMob and paste its id over the TODO in `AdBannerView.kt`.
     * 2. Confirm `remove_ads` is live in the Play console — see [docs/ads.md].
     * 3. Set this to `false`, then delete it and the `comingSoon` branches in `GameConfigScreen`
     *    that read it. The real wiring underneath was never removed, so nothing else changes.
     * 4. Drop the `coming_soon` string if nothing else uses it, and clear the note in
     *    `docs/ads.md` and `docs/question-packs.md`.
     *
     * The sheet itself is left standing on purpose: it is the design's, it is finished, and
     * seeing it lets the value of the premium tier land before the tier can be bought.
     */
    const val PACK_UNLOCK_COMING_SOON = true
}
