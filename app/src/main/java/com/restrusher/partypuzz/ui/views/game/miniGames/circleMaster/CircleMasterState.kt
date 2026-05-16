package com.restrusher.partypuzz.ui.views.game.miniGames.circleMaster

import androidx.compose.ui.geometry.Offset
import com.restrusher.partypuzz.data.models.Player

enum class CircleMasterPhase { COUNTDOWN, DRAWING, SCORE_REVEAL, PASS, WINNER }

data class CircleMasterState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val scores: Map<String, Int> = emptyMap(),
    val phase: CircleMasterPhase = CircleMasterPhase.COUNTDOWN,
    val countdownValue: Int = 3,
    val drawnPath: List<Offset> = emptyList(),
    val currentScore: Int? = null,
    val winner: Player? = null,
    val loser: Player? = null
) {
    val currentPlayer: Player? get() = players.getOrNull(currentPlayerIndex)
    val nextPlayer: Player? get() = players.getOrNull(currentPlayerIndex + 1)
    val isLastPlayer: Boolean get() = currentPlayerIndex >= players.size - 1
}
