package com.restrusher.partypuzl.ui.common.ads.meta

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.restrusher.partypuzl.BuildConfig
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Meta Audience Network: banners, rewarded ads, and an interstitial when a game ends. */
@Singleton
class MetaProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialManager: MetaInterstitialManager
) : AdsProvider {
    override val isFullScreenAdVisible get() = interstitialManager.isAdVisible
    override val bannerHeight: Dp = 50.dp

    override fun initialize() {
        AdSettings.setTestMode(BuildConfig.USE_TEST_ADS)
        AudienceNetworkAds.initialize(context)
        interstitialManager.loadAd()
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped() = Unit

    override fun onGameSessionEnded(activity: Activity) = interstitialManager.showIfReady()

    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) {
        val viewContext = LocalContext.current
        val adView = remember(placement) {
            AdView(viewContext, MetaPlacementIds.of(placement), AdSize.BANNER_HEIGHT_50)
        }
        DisposableEffect(adView) {
            adView.loadAd()
            onDispose { adView.destroy() }
        }
        AndroidView(modifier = modifier, factory = { adView })
    }

    @Composable
    override fun rememberRewarded(placement: AdPlacement): RewardedAdHandle {
        val viewContext = LocalContext.current
        val ad = remember(placement) { MetaRewardedAd(viewContext, MetaPlacementIds.of(placement)) }
        DisposableEffect(ad) {
            ad.reload()
            onDispose { ad.destroy() }
        }
        return ad
    }
}
