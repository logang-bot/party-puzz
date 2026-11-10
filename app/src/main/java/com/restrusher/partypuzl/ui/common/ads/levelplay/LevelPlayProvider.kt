package com.restrusher.partypuzl.ui.common.ads.levelplay

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.BuildConfig
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayConfiguration
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayInitListener
import com.unity3d.mediation.LevelPlayInitRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unity LevelPlay (formerly ironSource) mediation. One SDK auctions every request between
 * ironSource Ads and Unity Ads and serves the winner: banners, rewarded, and a game-end interstitial.
 */
@Singleton
class LevelPlayProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialManager: LevelPlayInterstitialManager
) : AdsProvider {
    override val isFullScreenAdVisible get() = interstitialManager.isAdVisible
    override val bannerHeight: Dp = 50.dp

    private var isSdkReady by mutableStateOf(false)

    private val initListener = object : LevelPlayInitListener {
        override fun onInitSuccess(configuration: LevelPlayConfiguration) = onSdkReady()
        override fun onInitFailed(error: LevelPlayInitError) = Unit
    }

    override fun initialize() {
        if (BuildConfig.USE_TEST_ADS) enableTestTools()
        LevelPlay.init(context, LevelPlayInitRequest.Builder(LevelPlayKeys.APP_KEY).build(), initListener)
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped() = Unit

    override fun onGameSessionEnded(activity: Activity) = interstitialManager.showIfReady(activity)

    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) {
        LevelPlayBanner(LevelPlayKeys.adUnitOf(placement), bannerHeight, isSdkReady, modifier)
    }

    @Composable
    override fun rememberRewarded(placement: AdPlacement): RewardedAdHandle {
        val handle = remember(placement) { LevelPlayRewardedHandle(LevelPlayKeys.adUnitOf(placement)) }
        DisposableEffect(handle, isSdkReady) {
            if (isSdkReady) handle.reload()
            onDispose { }
        }
        return handle
    }

    private fun enableTestTools() {
        LevelPlay.setAdaptersDebug(true)
        LevelPlay.setMetaData("is_test_suite", "enable")
    }

    private fun onSdkReady() {
        isSdkReady = true
        interstitialManager.loadAd()
    }
}
