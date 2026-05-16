package com.restrusher.partypuzz.ui.views.game.miniGames.simonSays

import com.restrusher.partypuzz.data.models.Player

enum class SimonSaysPhase { COUNTDOWN, SHOWING, INPUT, PASS, GAME_OVER }

data class SimonSaysState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val sequence: List<Int> = emptyList(),
    val playerInputIndex: Int = 0,
    val phase: SimonSaysPhase = SimonSaysPhase.COUNTDOWN,
    val highlightedButton: Int = -1,
    val countdownValue: Int = 3,
    val loser: Player? = null
) {
    val currentPlayer: Player? get() = players.getOrNull(currentPlayerIndex)
    val roundNumber: Int get() = sequence.size
}
