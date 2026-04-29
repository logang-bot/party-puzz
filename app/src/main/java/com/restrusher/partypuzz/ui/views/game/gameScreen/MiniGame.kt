package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.annotation.StringRes
import com.restrusher.partypuzz.R

enum class MiniGame(
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val minPlayers: Int,
    val isGlobal: Boolean = false
) {
    FOLLOW_THE_SPOT(R.string.follow_the_spot, R.string.follow_the_spot_description, minPlayers = 2),
    HOT_POTATO(R.string.hot_potato, R.string.hot_potato_description, minPlayers = 2, isGlobal = true),
    TAP_WAR(R.string.tap_war, R.string.tap_war_description, minPlayers = 2),
    SIMON_SAYS(R.string.simon_says, R.string.simon_says_description, minPlayers = 2, isGlobal = true)
}
