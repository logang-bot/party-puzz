package com.restrusher.partypuzz.ui.views.game

data class FollowTheSpotState(
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val timeRemaining: Int = 15,
    val player1SpotNormX: Float = 0.5f,
    val player1SpotNormY: Float = 0.5f,
    val player2SpotNormX: Float = 0.5f,
    val player2SpotNormY: Float = 0.5f,
    val isGameRunning: Boolean = false
)
