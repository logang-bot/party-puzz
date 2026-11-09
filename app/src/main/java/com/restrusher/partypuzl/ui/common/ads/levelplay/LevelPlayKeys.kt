package com.restrusher.partypuzl.ui.common.ads.levelplay

import com.restrusher.partypuzl.ui.common.ads.AdPlacement

/**
 * Unity LevelPlay keys. The app key comes from LevelPlay → Apps; each ad unit id from LevelPlay →
 * Setup → Ad Units. Meta placement ids never appear here: they live in the Meta network setup inside
 * the LevelPlay dashboard. See docs/ads-levelplay.md.
 */
object LevelPlayKeys {
    // TODO: replace every placeholder with the real value from the LevelPlay dashboard.
    const val APP_KEY = "YOUR_LEVELPLAY_APP_KEY"
    const val GAME_END_INTERSTITIAL = "YOUR_GAME_END_INTERSTITIAL_AD_UNIT_ID"

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
