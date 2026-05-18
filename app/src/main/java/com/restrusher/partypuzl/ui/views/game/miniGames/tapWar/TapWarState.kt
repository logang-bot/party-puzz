package com.restrusher.partypuzl.ui.views.game.miniGames.tapWar

import com.restrusher.partypuzl.data.models.Player

data class TapWarState(
    val player1: Player? = null,
    val player2: Player? = null,
    val barPosition: Float = 0.5f,
    val isGameRunning: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownValue: Int = 3,
    val timeRemaining: Int = 0,
    val winner: Int? = null
)
