package com.restrusher.partypuzz.ui.views.game.gameScreen

data class ActiveStickyDare(
    val id: String,
    val playerName: String,
    val presentContinuousText: String,
    val durationLabel: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isCompleted: Boolean = false
)

fun Int.toRemainingTimeLabel(): String = when {
    this >= 120 -> "${this / 60} minutes"
    this >= 60 -> "1 minute"
    this == 1 -> "1 second"
    else -> "$this seconds"
}
