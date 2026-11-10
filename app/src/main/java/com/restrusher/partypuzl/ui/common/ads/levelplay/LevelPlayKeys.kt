package com.restrusher.partypuzl.ui.common.ads.levelplay

import com.restrusher.partypuzl.ui.common.ads.AdPlacement

/**
 * Unity LevelPlay keys. The app key comes from LevelPlay → Apps; each ad unit id from LevelPlay →
 * Setup → Ad Units. See docs/ads-levelplay.md.
 */
object LevelPlayKeys {
    const val APP_KEY = "29712da25"
    const val GAME_END_INTERSTITIAL = "4t9hlie2nennlxmc"

    private val adUnitIds = mapOf(
        AdPlacement.HOME_BANNER to "9lldu1d2i8moippz",
        AdPlacement.PARTIES_BANNER to "6m7l1awolk2qscrb",
        AdPlacement.PARTY_DETAIL_BANNER to "vrvyn54aufz2o856",
        AdPlacement.SETTINGS_BANNER to "khw6ghoul4qkrkbp",
        AdPlacement.GAME_CONFIG_BANNER to "qnglag1k04fklza9",
        AdPlacement.PACK_UNLOCK_REWARDED to "461k6cho7k1iadso"
    )

    fun adUnitOf(placement: AdPlacement): String = adUnitIds.getValue(placement)
}
