package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import com.restrusher.partypuzz.data.models.Player

data class FollowTheSpotState(
    val player1: Player? = null,
    val player2: Player? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val timeRemaining: Int = 15,
    val player1SpotNormX: Float = 0.5f,
    val player1SpotNormY: Float = 0.5f,
    val player2SpotNormX: Float = 0.5f,
    val player2SpotNormY: Float = 0.5f,
    val isGameRunning: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownValue: Int = 3
)
