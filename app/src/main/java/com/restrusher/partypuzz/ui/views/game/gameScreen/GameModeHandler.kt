package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

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
        val result = state.miniGameResult ?: return state
        val event: BarEvent = when (result.winner) {
            state.selectedPlayer?.nickName -> BarModeState.giveDrinksEvent(
                targetPlayerName = state.miniGameOpponent?.nickName.orEmpty()
            )
            null -> BarEvent.NoAction
            else -> BarModeState.takeDrinksEvent()
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
        val result = state.miniGameResult ?: return state
        if (result.winner != state.selectedPlayer?.nickName) return state
        return state.copy(couplesMode = state.couplesMode.copy(activeEvent = CouplesModeState.rewardEvent()))
    }

    override fun clearEvent(state: GameScreenState) =
        state.copy(couplesMode = state.couplesMode.copy(activeEvent = null))
}
