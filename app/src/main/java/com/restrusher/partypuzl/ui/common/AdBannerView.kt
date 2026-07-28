package com.restrusher.partypuzl.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.restrusher.partypuzl.BuildConfig

val LocalIsAdFree = compositionLocalOf { false }

object AdUnitIds {
    private object Test {
        const val HOME_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val PARTIES_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val PARTY_DETAIL_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val SETTINGS_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val GAME_CONFIG_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
        const val PACK_UNLOCK_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }

    private object Production {
        const val HOME_BANNER = "ca-app-pub-7803968519747509/3694554226"
        const val PARTIES_BANNER = "ca-app-pub-7803968519747509/5219388884"
        const val PARTY_DETAIL_BANNER = "ca-app-pub-7803968519747509/2253936898"
        const val SETTINGS_BANNER = "ca-app-pub-7803968519747509/7340639822"
        const val GAME_CONFIG_BANNER = "ca-app-pub-7803968519747509/8801061155"
        const val APP_OPEN = "ca-app-pub-7803968519747509/2197639728"

        // TODO: create a Rewarded ad unit in the AdMob dashboard and paste its id here.
        // Until then this points at Google's test rewarded unit, so the unlock flow works
        // end to end in release builds but earns nothing.
        const val PACK_UNLOCK_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }

    val HOME_BANNER get() = if (BuildConfig.USE_TEST_ADS) Test.HOME_BANNER else Production.HOME_BANNER
    val PARTIES_BANNER get() = if (BuildConfig.USE_TEST_ADS) Test.PARTIES_BANNER else Production.PARTIES_BANNER
    val PARTY_DETAIL_BANNER get() = if (BuildConfig.USE_TEST_ADS) Test.PARTY_DETAIL_BANNER else Production.PARTY_DETAIL_BANNER
    val SETTINGS_BANNER get() = if (BuildConfig.USE_TEST_ADS) Test.SETTINGS_BANNER else Production.SETTINGS_BANNER
    val GAME_CONFIG_BANNER get() = if (BuildConfig.USE_TEST_ADS) Test.GAME_CONFIG_BANNER else Production.GAME_CONFIG_BANNER
    val APP_OPEN get() = if (BuildConfig.USE_TEST_ADS) Test.APP_OPEN else Production.APP_OPEN
    val PACK_UNLOCK_REWARDED get() = if (BuildConfig.USE_TEST_ADS) Test.PACK_UNLOCK_REWARDED else Production.PACK_UNLOCK_REWARDED
}

@Composable
fun AdBannerView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    if (LocalIsAdFree.current) return
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
