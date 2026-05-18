package com.restrusher.partypuzl.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// TODO: Replace with your production ad unit IDs from the AdMob dashboard
// TODO: Replace each value with the matching production ad unit ID from the AdMob dashboard
object AdUnitIds {
    // Test IDs — safe to use during development, never serve real ads
    const val HOME_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val PARTIES_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val PARTY_DETAIL_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val SETTINGS_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val GAME_CONFIG_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
}

@Composable
fun AdBannerView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
