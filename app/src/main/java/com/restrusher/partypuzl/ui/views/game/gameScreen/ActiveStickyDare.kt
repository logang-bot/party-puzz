package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.restrusher.partypuzl.R

data class ActiveStickyDare(
    val id: String,
    val playerName: String,
    val presentContinuousText: String,
    val durationLabel: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isCompleted: Boolean = false
)

@Composable
fun Int.toRemainingTimeLabel(): String = when {
    this >= 120 -> {
        val minutes = this / 60
        pluralStringResource(R.plurals.remaining_minutes, minutes, minutes)
    }
    this >= 60 -> pluralStringResource(R.plurals.remaining_minutes, 1, 1)
    else -> pluralStringResource(R.plurals.remaining_seconds, this, this)
}
