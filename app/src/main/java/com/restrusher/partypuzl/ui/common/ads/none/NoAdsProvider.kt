package com.restrusher.partypuzl.ui.common.ads.none

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import javax.inject.Inject

/** The provider `main` ships with: no SDK, no requests, nothing on screen. */
class NoAdsProvider @Inject constructor() : AdsProvider {
    override val isFullScreenAdVisible = false
    override val bannerHeight: Dp = 0.dp

    override fun initialize() = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped() = Unit
    override fun onGameSessionEnded(activity: Activity) = Unit

    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) = Unit

    @Composable
    override fun rememberRewarded(placement: AdPlacement): RewardedAdHandle = NeverReadyRewardedAd

    private object NeverReadyRewardedAd : RewardedAdHandle {
        override val isReady = false
        override fun reload() = Unit
        override fun show(activity: Activity, onRewarded: () -> Unit) = Unit
    }
}
