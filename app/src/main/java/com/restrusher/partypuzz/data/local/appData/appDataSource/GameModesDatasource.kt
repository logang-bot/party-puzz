package com.restrusher.partypuzz.data.local.appData.appDataSource

import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appModels.GameMode

object GameModesDatasource {
    val gameModesList = listOf(
        GameMode(
            R.drawable.img_solo_mode_illustration,
            R.string.solo_game_mode,
            R.string.solo_description
        ),
        GameMode(
            R.drawable.img_couples_mode_illustration,
            R.string.couples_game_mode,
            R.string.couples_description
        ),
        GameMode(
            R.drawable.img_teams_mode_illustration,
            R.string.teams_game_mode,
            R.string.teams_description
        ),
        GameMode(
            R.drawable.img_partypuzz_mode_illustration,
            R.string.party_puzz_game_mode,
            R.string.party_puzz_description
        ),
    )
}