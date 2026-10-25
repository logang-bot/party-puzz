package com.restrusher.partypuzl.ui.common.ads.meta

import com.restrusher.partypuzl.ui.common.ads.AdPlacement

/**
 * Meta Audience Network placement ids, each `<APP_ID>_<PLACEMENT_ID>` from Monetization Manager.
 * Meta has no separate test ids: debug and staging use these same ids with test mode switched on
 * in [MetaProvider.initialize].
 */
object MetaPlacementIds {
    // TODO: replace every placeholder with the real id from Monetization Manager → Placements.
    const val GAME_END_INTERSTITIAL = "YOUR_APP_ID_GAME_END_INTERSTITIAL"

    private val ids = mapOf(
        AdPlacement.HOME_BANNER to "YOUR_APP_ID_HOME_BANNER",
        AdPlacement.PARTIES_BANNER to "YOUR_APP_ID_PARTIES_BANNER",
        AdPlacement.PARTY_DETAIL_BANNER to "YOUR_APP_ID_PARTY_DETAIL_BANNER",
        AdPlacement.SETTINGS_BANNER to "YOUR_APP_ID_SETTINGS_BANNER",
        AdPlacement.GAME_CONFIG_BANNER to "YOUR_APP_ID_GAME_CONFIG_BANNER",
        AdPlacement.PACK_UNLOCK_REWARDED to "YOUR_APP_ID_PACK_UNLOCK_REWARDED"
    )

    fun of(placement: AdPlacement): String = ids.getValue(placement)
}
