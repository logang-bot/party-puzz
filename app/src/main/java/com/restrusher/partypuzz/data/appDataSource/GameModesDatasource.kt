package com.restrusher.partypuzz.data.appDataSource

import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appModels.GameMode


object GameModesDatasource {
    val gameModesList = listOf(
        GameMode(
            R.drawable.ic_launcher_foreground,
            R.string.solo_game_mode,
            R.string.solo_description
        ),
        GameMode(
            R.drawable.ic_launcher_foreground,
            R.string.couples_game_mode,
            R.string.couples_description
        ),
        GameMode(
            R.drawable.ic_launcher_foreground,
            R.string.teams_game_mode,
            R.string.teams_description
        ),
        GameMode(
            R.drawable.ic_launcher_foreground,
            R.string.party_puzz_game_mode,
            R.string.party_puzz_description
        ),
    )
}