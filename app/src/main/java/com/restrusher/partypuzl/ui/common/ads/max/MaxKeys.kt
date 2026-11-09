package com.restrusher.partypuzl.ui.common.ads.max

import com.restrusher.partypuzl.ui.common.ads.AdPlacement

/**
 * AppLovin MAX keys. The SDK key comes from AppLovin → Account → Keys; each ad unit id from
 * MAX → Manage → Ad Units. Meta placement ids never appear here: they live in each MAX ad unit's
 * network settings. See docs/ads-max.md.
 */
object MaxKeys {
    // TODO: replace every placeholder with the real value from the AppLovin dashboard.
    const val SDK_KEY = "YOUR_APPLOVIN_SDK_KEY"
    const val GAME_END_INTERSTITIAL = "YOUR_GAME_END_INTERSTITIAL_AD_UNIT_ID"

    /** Advertising IDs of your own devices. Debug and staging builds receive test ads on them. */
    val TEST_DEVICE_ADVERTISING_IDS: List<String> = emptyList()

    private val adUnitIds = mapOf(
        AdPlacement.HOME_BANNER to "YOUR_HOME_BANNER_AD_UNIT_ID",
        AdPlacement.PARTIES_BANNER to "YOUR_PARTIES_BANNER_AD_UNIT_ID",
        AdPlacement.PARTY_DETAIL_BANNER to "YOUR_PARTY_DETAIL_BANNER_AD_UNIT_ID",
        AdPlacement.SETTINGS_BANNER to "YOUR_SETTINGS_BANNER_AD_UNIT_ID",
        AdPlacement.GAME_CONFIG_BANNER to "YOUR_GAME_CONFIG_BANNER_AD_UNIT_ID",
        AdPlacement.PACK_UNLOCK_REWARDED to "YOUR_PACK_UNLOCK_REWARDED_AD_UNIT_ID"
    )

    fun adUnitOf(placement: AdPlacement): String = adUnitIds.getValue(placement)
}
