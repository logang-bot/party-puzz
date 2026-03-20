package com.restrusher.partypuzz.ui.views.game

import androidx.annotation.StringRes
import com.restrusher.partypuzz.R

enum class MiniGame(
    @StringRes val nameRes: Int,
    val minPlayers: Int
) {
    FOLLOW_THE_SPOT(R.string.follow_the_spot, minPlayers = 2)
}
