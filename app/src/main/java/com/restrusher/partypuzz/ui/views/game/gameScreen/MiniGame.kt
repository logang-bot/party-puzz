package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.annotation.StringRes
import com.restrusher.partypuzz.R

enum class MiniGame(
    @StringRes val nameRes: Int,
    val minPlayers: Int,
    val isGlobal: Boolean = false
) {
    FOLLOW_THE_SPOT(R.string.follow_the_spot, minPlayers = 2),
    HOT_POTATO(R.string.hot_potato, minPlayers = 2, isGlobal = true)
}
