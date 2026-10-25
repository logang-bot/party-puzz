package com.restrusher.partypuzl.ui.common.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.ui.common.ads.none.NoAdsProvider

val LocalIsAdFree = compositionLocalOf { false }

val LocalAdsProvider = staticCompositionLocalOf<AdsProvider> { NoAdsProvider() }

@Composable
fun AdBanner(
    placement: AdPlacement,
    modifier: Modifier = Modifier
) {
    if (LocalIsAdFree.current) return
    LocalAdsProvider.current.Banner(placement, modifier.fillMaxWidth())
}

@Composable
fun adBannerReservedHeight(): Dp =
    if (LocalIsAdFree.current) 0.dp else LocalAdsProvider.current.bannerHeight

@Composable
fun rememberRewardedAd(placement: AdPlacement): RewardedAdHandle =
    LocalAdsProvider.current.rememberRewarded(placement)

/** Wraps a game's exit so the provider gets its between-sessions ad slot before leaving. */
@Composable
fun rememberGameSessionExit(onExit: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val provider = LocalAdsProvider.current
    return remember(context, provider, onExit) {
        {
            context.findActivity()?.let(provider::onGameSessionEnded)
            onExit()
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
