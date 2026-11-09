package com.restrusher.partypuzl.ui.common.ads.max

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.applovin.mediation.ads.MaxAdView

/**
 * A fixed-height MAX banner. Adaptive sizing is switched off so the banner always matches the
 * bottom padding screens reserve through `adBannerReservedHeight()`.
 */
@Composable
fun MaxBanner(
    adUnitId: String,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adView = remember(adUnitId) {
        MaxAdView(adUnitId, context).apply { setExtraParameter("adaptive_banner", "false") }
    }
    DisposableEffect(adView) {
        adView.loadAd()
        onDispose { adView.destroy() }
    }
    AndroidView(modifier = modifier.height(height), factory = { adView })
}
