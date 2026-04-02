package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

data class BarModeState(
    val isActive: Boolean = false,
    val activeEvent: BarEvent? = null
) {
    companion object {
        fun takeDrinksEvent(): BarEvent.TakeDrinks =
            BarEvent.TakeDrinks(amount = (1..5).random())

        fun giveDrinksEvent(targetPlayerName: String): BarEvent.GiveDrinks =
            BarEvent.GiveDrinks(amount = (1..5).random(), targetPlayerName = targetPlayerName)

        fun giveDrinksPickTargetEvent(
            players: List<Player>,
            currentPlayer: Player?
        ): BarEvent.GiveDrinksPickTarget {
            val candidates = players
                .filter { it.id != currentPlayer?.id }
                .map { it.nickName }
            return BarEvent.GiveDrinksPickTarget(amount = (1..5).random(), candidates = candidates)
        }
    }
}
