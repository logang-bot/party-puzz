package com.restrusher.partypuzz.data.local.appData.appDataSource

import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appModels.GameMode

object GameModesDatasource {
    val gameModesList = listOf(
        GameMode(
            R.drawable.ic_standard,
            R.string.standard_game_mode,
            R.string.standard_description
        ),
        GameMode(
            R.drawable.ic_couples,
            R.string.couples_game_mode,
            R.string.couples_description
        ),
        GameMode(
            R.drawable.ic_bar,
            R.string.bar_game_mode,
            R.string.bar_description
        ),
        GameMode(
            R.drawable.ic_partypuzz,
            R.string.party_puzz_game_mode,
            R.string.party_puzz_description
        ),
    )
}
