package com.restrusher.partypuzz.ui.views.game.miniGames.hotPotato

import com.restrusher.partypuzz.data.models.Player

data class HotPotatoState(
    val players: List<Player> = emptyList(),
    val currentHolderIndex: Int = 0,
    val isGameRunning: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownValue: Int = 3,
    val loserIndex: Int? = null
) {
    val currentHolder: Player? get() = players.getOrNull(currentHolderIndex)
    val nextHolder: Player? get() = if (players.size > 1)
        players[(currentHolderIndex + 1) % players.size] else null
    val loser: Player? get() = loserIndex?.let { players.getOrNull(it) }
}
