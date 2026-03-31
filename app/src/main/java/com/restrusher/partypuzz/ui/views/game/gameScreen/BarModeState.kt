package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

data class BarModeState(
    val isActive: Boolean = false,
    val activeEvent: BarEvent? = null
) {
    companion object {
        fun triggerRandomEvent(players: List<Player>, currentPlayer: Player?): BarEvent {
            val roll = (1..10).random()
            return when {
                roll <= 4 -> BarEvent.NoAction
                roll <= 7 -> {
                    val target = players
                        .filter { it.id != currentPlayer?.id }
                        .randomOrNull() ?: return BarEvent.NoAction
                    BarEvent.GiveDrinks(
                        amount = (1..5).random(),
                        targetPlayerName = target.nickName
                    )
                }
                else -> BarEvent.TakeDrinks(amount = (1..5).random())
            }
        }
    }
}
