package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player

data class CouplesModeState(
    val isActive: Boolean = false,
    val activeEvent: CouplesEvent? = null
) {
    companion object {
        fun punishmentEvent(players: List<Player>, currentPlayer: Player?): CouplesEvent {
            val others = players.filter { it.id != currentPlayer?.id }

            val interestedMatches = if (currentPlayer == null) {
                others
            } else {
                others.filter { target ->
                    target.gender == Gender.Unknown || when (currentPlayer.interestedIn) {
                        InterestedIn.Man -> target.gender == Gender.Male
                        InterestedIn.Woman -> target.gender == Gender.Female
                        InterestedIn.Both -> true
                    }
                }
            }

            val pool = interestedMatches.ifEmpty { others }
            val target = pool.randomOrNull()?.nickName.orEmpty()

            return if ((0..1).random() == 0) {
                CouplesEvent.MakeALoveDeclaration(targetPlayerName = target)
            } else {
                CouplesEvent.ActOfLove(requesterPlayerName = target)
            }
        }

        fun rewardEvent(): CouplesEvent = when ((0..2).random()) {
            0 -> CouplesEvent.GiveAKiss
            1 -> CouplesEvent.ChooseKissers
            else -> CouplesEvent.ChooseLovers
        }
    }
}
