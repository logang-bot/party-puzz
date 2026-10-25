package com.restrusher.partypuzl.ui.common.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * The only door between the app and an ad SDK. Screens and lifecycle code talk to this; the
 * implementation bound in [AdsModule] decides which network — if any — answers.
 *
 * `main` binds [com.restrusher.partypuzl.ui.common.ads.none.NoAdsProvider]. The `ads/admob` and
 * `ads/meta` branches add their own provider package and change that one binding. See docs/ads.md.
 */
interface AdsProvider {
    /** Compose state. True while a full-screen ad covers the app, so `MainActivity` can mask it. */
    val isFullScreenAdVisible: Boolean

    /** Height a screen reserves at its bottom edge for a pinned banner. */
    val bannerHeight: Dp

    fun initialize()
    fun onActivityStarted(activity: Activity)
    fun onActivityStopped()
    fun onGameSessionEnded(activity: Activity)

    @Composable
    fun Banner(placement: AdPlacement, modifier: Modifier)

    @Composable
    fun rememberRewarded(placement: AdPlacement): RewardedAdHandle
}
