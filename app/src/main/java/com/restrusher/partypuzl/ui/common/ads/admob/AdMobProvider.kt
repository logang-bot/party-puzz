package com.restrusher.partypuzl.ui.common.ads.admob

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Google AdMob: banners, an App Open ad on every launch, and rewarded ads. */
@Singleton
class AdMobProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appOpenManager: AdMobAppOpenManager
) : AdsProvider {
    override val isFullScreenAdVisible get() = appOpenManager.isAdVisible
    override val bannerHeight: Dp = 50.dp

    override fun initialize() {
        MobileAds.initialize(context)
        appOpenManager.loadAd()
    }

    override fun onActivityStarted(activity: Activity) = appOpenManager.showWhenReady(activity)

    override fun onActivityStopped() = appOpenManager.clearPendingActivity()

    override fun onGameSessionEnded(activity: Activity) = Unit

    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobUnitIds.of(placement)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }

    @Composable
    override fun rememberRewarded(placement: AdPlacement): RewardedAdHandle {
        val viewContext = LocalContext.current
        val ad = remember(placement) { AdMobRewardedAd(viewContext, AdMobUnitIds.of(placement)) }
        LaunchedEffect(ad) { ad.reload() }
        return ad
    }
}
