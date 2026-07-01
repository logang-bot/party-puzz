package com.restrusher.partypuzl.ui.common

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

val LocalIsAdFree = compositionLocalOf { false }

object AdPlacementIds {
    const val HOME_BANNER = "Banner_Android"
    const val PARTIES_BANNER = "Banner_Android"
    const val PARTY_DETAIL_BANNER = "Banner_Android"
    const val SETTINGS_BANNER = "Banner_Android"
    const val GAME_CONFIG_BANNER = "Banner_Android"
    const val APP_OPEN_INTERSTITIAL = "Interstitial_Android"
    const val GAME_CONFIG_REWARDED = "Rewarded_Android"
}

@Composable
fun AdBannerView(
    placementId: String,
    modifier: Modifier = Modifier
) {
    if (LocalIsAdFree.current) return
    val activity = LocalContext.current as Activity
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            BannerView(activity, placementId, UnityBannerSize(320, 50)).apply {
                setListener(object : BannerView.IListener {
                    override fun onBannerLoaded(bannerAdView: BannerView) {
                        Log.d("AdBannerView", "Banner loaded: $placementId")
                    }
                    override fun onBannerShown(bannerAdView: BannerView?) {
                        Log.d("AdBannerView", "Banner shown: $placementId")
                    }
                    override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
                        Log.e(
                            "AdBannerView",
                            "Banner failed to load ($placementId): [${errorInfo.errorCode}] ${errorInfo.errorMessage}"
                        )
                    }
                    override fun onBannerClick(bannerAdView: BannerView) {}
                    override fun onBannerLeftApplication(bannerAdView: BannerView) {}
                })
                load()
            }
        },
        onRelease = { it.destroy() }
    )
}
