package com.restrusher.partypuzl.ui.common.ads.levelplay

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.mediation.LevelPlayAdSize
import com.unity3d.mediation.banner.LevelPlayBannerAdView

/**
 * A fixed 320×50 LevelPlay banner, so it always matches the bottom padding screens reserve through
 * `adBannerReservedHeight()`. LevelPlay ad objects may only exist after init succeeds, so until
 * [isSdkReady] this holds the space empty.
 */
@Composable
fun LevelPlayBanner(
    adUnitId: String,
    height: Dp,
    isSdkReady: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isSdkReady) {
        Spacer(modifier.height(height))
        return
    }
    val context = LocalContext.current
    val adView = remember(adUnitId) {
        val config = LevelPlayBannerAdView.Config.Builder().setAdSize(LevelPlayAdSize.BANNER).build()
        LevelPlayBannerAdView(context, adUnitId, config).apply { setBannerListener(LevelPlayBannerLogger) }
    }
    DisposableEffect(adView) {
        adView.loadAd()
        onDispose { adView.destroy() }
    }
    AndroidView(modifier = modifier.height(height), factory = { adView })
}
