package com.restrusher.partypuzl.ui.common.ads.max

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.restrusher.partypuzl.BuildConfig
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppLovin MAX mediation. One SDK auctions every request between AppLovin and the mediated networks
 * (Meta Audience Network today) and serves the winner: banners, rewarded, and a game-end
 * interstitial.
 */
@Singleton
class MaxProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialManager: MaxInterstitialManager
) : AdsProvider {
    override val isFullScreenAdVisible get() = interstitialManager.isAdVisible
    override val bannerHeight: Dp = 50.dp

    override fun initialize() {
        val sdk = AppLovinSdk.getInstance(context)
        sdk.settings.setVerboseLogging(BuildConfig.USE_TEST_ADS)
        sdk.initialize(initializationConfiguration()) { interstitialManager.loadAd() }
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped() = Unit

    override fun onGameSessionEnded(activity: Activity) = interstitialManager.showIfReady(activity)

    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) {
        MaxBanner(MaxKeys.adUnitOf(placement), bannerHeight, modifier)
    }

    @Composable
    override fun rememberRewarded(placement: AdPlacement): RewardedAdHandle {
        val handle = remember(placement) { MaxRewardedHandle(MaxKeys.adUnitOf(placement)) }
        DisposableEffect(handle) {
            handle.reload()
            onDispose { handle.detach() }
        }
        return handle
    }

    private fun initializationConfiguration(): AppLovinSdkInitializationConfiguration {
        val testDevices = if (BuildConfig.USE_TEST_ADS) MaxKeys.TEST_DEVICE_ADVERTISING_IDS else emptyList()
        return AppLovinSdkInitializationConfiguration.builder(MaxKeys.SDK_KEY)
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .setTestDeviceAdvertisingIds(testDevices)
            .build()
    }
}
