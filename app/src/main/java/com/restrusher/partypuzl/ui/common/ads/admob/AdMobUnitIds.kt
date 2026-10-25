package com.restrusher.partypuzl.ui.common.ads.admob

import com.restrusher.partypuzl.BuildConfig
import com.restrusher.partypuzl.ui.common.ads.AdPlacement

/** AdMob unit ids per [AdPlacement]. Debug and staging get Google's test units (`USE_TEST_ADS`). */
object AdMobUnitIds {
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    private const val PRODUCTION_APP_OPEN = "ca-app-pub-7803968519747509/2197639728"

    private val production = mapOf(
        AdPlacement.HOME_BANNER to "ca-app-pub-7803968519747509/3694554226",
        AdPlacement.PARTIES_BANNER to "ca-app-pub-7803968519747509/5219388884",
        AdPlacement.PARTY_DETAIL_BANNER to "ca-app-pub-7803968519747509/2253936898",
        AdPlacement.SETTINGS_BANNER to "ca-app-pub-7803968519747509/7340639822",
        AdPlacement.GAME_CONFIG_BANNER to "ca-app-pub-7803968519747509/8801061155",
        // TODO: create a Rewarded ad unit in the AdMob dashboard and paste its id here.
        // Until then this is Google's test rewarded unit, so the unlock earns nothing.
        AdPlacement.PACK_UNLOCK_REWARDED to TEST_REWARDED
    )

    val appOpen get() = if (BuildConfig.USE_TEST_ADS) TEST_APP_OPEN else PRODUCTION_APP_OPEN

    fun of(placement: AdPlacement): String =
        if (BuildConfig.USE_TEST_ADS) testIdOf(placement) else production.getValue(placement)

    private fun testIdOf(placement: AdPlacement) =
        if (placement == AdPlacement.PACK_UNLOCK_REWARDED) TEST_REWARDED else TEST_BANNER
}
