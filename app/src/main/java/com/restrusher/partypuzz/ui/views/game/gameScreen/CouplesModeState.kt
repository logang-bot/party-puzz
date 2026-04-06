package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

data class CouplesModeState(
    val isActive: Boolean = false,
    val activeEvent: CouplesEvent? = null
) {
    companion object {
        fun punishmentEvent(players: List<Player>, currentPlayer: Player?): CouplesEvent {
            val target = players
                .filter { it.id != currentPlayer?.id }
                .randomOrNull()
                ?.nickName
                .orEmpty()
            return if ((0..1).random() == 0) {
                CouplesEvent.MakeALoveDeclaration(targetPlayerName = target)
            } else {
                CouplesEvent.ActOfLove(requesterPlayerName = target)
            }
        }

        fun rewardEvent(): CouplesEvent = when ((0..2).random()) {
            0 -> CouplesEvent.GiveAKiss
            1 -> CouplesEvent.ChoseKissers
            else -> CouplesEvent.ChoseLovers
        }
    }
}
