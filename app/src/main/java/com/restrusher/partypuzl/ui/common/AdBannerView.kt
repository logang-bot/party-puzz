package com.restrusher.partypuzl.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.restrusher.partypuzl.BuildConfig

object AdUnitIds {
    private object Test {
        const val HOME_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val PARTIES_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val PARTY_DETAIL_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val SETTINGS_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val GAME_CONFIG_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    }

    private object Production {
        const val HOME_BANNER = "ca-app-pub-7803968519747509/3694554226"
        const val PARTIES_BANNER = "ca-app-pub-7803968519747509/5219388884"
        const val PARTY_DETAIL_BANNER = "ca-app-pub-7803968519747509/2253936898"
        const val SETTINGS_BANNER = "ca-app-pub-7803968519747509/7340639822"
        const val GAME_CONFIG_BANNER = "ca-app-pub-7803968519747509/8801061155"
        const val APP_OPEN = "ca-app-pub-7803968519747509/2197639728"
    }

    val HOME_BANNER get() = if (BuildConfig.DEBUG) Test.HOME_BANNER else Production.HOME_BANNER
    val PARTIES_BANNER get() = if (BuildConfig.DEBUG) Test.PARTIES_BANNER else Production.PARTIES_BANNER
    val PARTY_DETAIL_BANNER get() = if (BuildConfig.DEBUG) Test.PARTY_DETAIL_BANNER else Production.PARTY_DETAIL_BANNER
    val SETTINGS_BANNER get() = if (BuildConfig.DEBUG) Test.SETTINGS_BANNER else Production.SETTINGS_BANNER
    val GAME_CONFIG_BANNER get() = if (BuildConfig.DEBUG) Test.GAME_CONFIG_BANNER else Production.GAME_CONFIG_BANNER
    val APP_OPEN get() = if (BuildConfig.DEBUG) Test.APP_OPEN else Production.APP_OPEN
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
