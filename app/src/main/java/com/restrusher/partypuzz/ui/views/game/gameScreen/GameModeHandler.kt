package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

enum class EventCategory { REWARD, PUNISHMENT }

internal interface GameModeHandler {
    fun applyPunishment(state: GameScreenState, currentPlayer: Player?): GameScreenState
    fun applyReward(state: GameScreenState): GameScreenState
    fun applyMiniGameResult(state: GameScreenState): GameScreenState
    fun clearEvent(state: GameScreenState): GameScreenState
}

internal class NoOpModeHandler : GameModeHandler {
    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) = state
    override fun applyReward(state: GameScreenState) = state
    override fun applyMiniGameResult(state: GameScreenState) = state
    override fun clearEvent(state: GameScreenState) = state
}

internal class BarModeHandler : GameModeHandler {
    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) =
        state.copy(barMode = state.barMode.copy(activeEvent = BarModeState.takeDrinksEvent()))

    override fun applyReward(state: GameScreenState) =
        state.copy(
            barMode = state.barMode.copy(
                activeEvent = BarModeState.giveDrinksPickTargetEvent(state.players, state.selectedPlayer)
            )
        )

    override fun applyMiniGameResult(state: GameScreenState): GameScreenState {
        val event: BarEvent = when (val result = state.miniGameResult) {
            is ScoredMiniGameResult -> when (result.winner) {
                state.selectedPlayer?.nickName -> BarModeState.giveDrinksEvent(
                    targetPlayerName = state.miniGameOpponent?.nickName.orEmpty()
                )
                null -> BarEvent.NoAction
                else -> BarModeState.takeDrinksEvent()
            }
            is LoserMiniGameResult -> BarModeState.takeDrinksEvent()
            null -> return state
        }
        return state.copy(barMode = state.barMode.copy(activeEvent = event))
    }

    override fun clearEvent(state: GameScreenState) =
        state.copy(barMode = state.barMode.copy(activeEvent = null))
}

internal class CouplesModeHandler : GameModeHandler {
    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) =
        state.copy(
            couplesMode = state.couplesMode.copy(
                activeEvent = CouplesModeState.punishmentEvent(state.players, currentPlayer)
            )
        )

    override fun applyReward(state: GameScreenState) =
        state.copy(couplesMode = state.couplesMode.copy(activeEvent = CouplesModeState.rewardEvent()))

    override fun applyMiniGameResult(state: GameScreenState): GameScreenState {
        return when (val result = state.miniGameResult) {
            is ScoredMiniGameResult -> when (result.winner) {
                state.selectedPlayer?.nickName -> applyReward(state)
                null -> state
                else -> applyPunishment(state, state.selectedPlayer)
            }
            is LoserMiniGameResult -> {
                val loser = state.players.find { it.nickName == result.loserName }
                applyPunishment(state, loser)
            }
            null -> state
        }
    }

    override fun clearEvent(state: GameScreenState) =
        state.copy(couplesMode = state.couplesMode.copy(activeEvent = null))
}

internal class PartyPuzzModeHandler : GameModeHandler {
    private val handlers = listOf(BarModeHandler(), CouplesModeHandler(), NoOpModeHandler())

    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) =
        handlers.random().applyPunishment(state, currentPlayer)

    override fun applyReward(state: GameScreenState) =
        handlers.random().applyReward(state)

    override fun applyMiniGameResult(state: GameScreenState) =
        handlers.random().applyMiniGameResult(state)

    override fun clearEvent(state: GameScreenState) = state.copy(
        barMode = state.barMode.copy(activeEvent = null),
        couplesMode = state.couplesMode.copy(activeEvent = null)
    )
}
