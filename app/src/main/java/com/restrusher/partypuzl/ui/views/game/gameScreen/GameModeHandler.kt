package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.barPunishmentDeck
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.barRewardDeck
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.couplesPunishmentDeck
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.couplesRewardDeck
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.deckLandingOn

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
        state.rollBarOutcome(barPunishmentDeck())

    override fun applyReward(state: GameScreenState) =
        state.rollBarOutcome(barRewardDeck(state.players, state.selectedPlayer))

    override fun applyMiniGameResult(state: GameScreenState): GameScreenState {
        val fired = state.barMiniGameEvent() ?: return state
        val deck = if (fired.category == EventCategory.PUNISHMENT) {
            barPunishmentDeck()
        } else {
            barRewardDeck(state.players, state.selectedPlayer)
        }
        return state.withBarOutcome(deckLandingOn(deck, fired), fired)
    }

    override fun clearEvent(state: GameScreenState) =
        state.copy(barMode = state.barMode.copy(activeEvent = null, deck = emptyList()))
}

private fun GameScreenState.rollBarOutcome(deck: List<BarEvent>): GameScreenState =
    withBarOutcome(deck, deck.random())

private fun GameScreenState.withBarOutcome(deck: List<BarEvent>, fired: BarEvent) =
    copy(barMode = barMode.copy(activeEvent = fired, deck = deck))

private fun GameScreenState.barMiniGameEvent(): BarEvent? = when (val result = miniGameResult) {
    is ScoredMiniGameResult -> when (result.winner) {
        selectedPlayer?.nickName ->
            BarModeState.giveDrinksEvent(targetPlayerName = miniGameOpponent?.nickName.orEmpty())
        null -> BarEvent.NoAction
        else -> BarModeState.takeDrinksEvent()
    }
    is LoserMiniGameResult -> BarModeState.takeDrinksEvent()
    null -> null
}

internal class CouplesModeHandler : GameModeHandler {
    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) =
        state.rollCouplesOutcome(couplesPunishmentDeck(state.players, currentPlayer))

    override fun applyReward(state: GameScreenState) =
        state.rollCouplesOutcome(couplesRewardDeck())

    override fun applyMiniGameResult(state: GameScreenState): GameScreenState =
        when (val result = state.miniGameResult) {
            is ScoredMiniGameResult -> scoredOutcome(state, result)
            is LoserMiniGameResult ->
                applyPunishment(state, state.players.find { it.nickName == result.loserName })
            null -> state
        }

    override fun clearEvent(state: GameScreenState) =
        state.copy(couplesMode = state.couplesMode.copy(activeEvent = null, deck = emptyList()))

    private fun scoredOutcome(state: GameScreenState, result: ScoredMiniGameResult) =
        when (result.winner) {
            state.selectedPlayer?.nickName -> applyReward(state)
            null -> state
            else -> applyPunishment(state, state.selectedPlayer)
        }
}

private fun GameScreenState.rollCouplesOutcome(deck: List<CouplesEvent>): GameScreenState =
    copy(couplesMode = couplesMode.copy(activeEvent = deck.random(), deck = deck))

internal class PartyPuzlModeHandler : GameModeHandler {
    private val handlers = listOf(BarModeHandler(), CouplesModeHandler(), NoOpModeHandler())

    override fun applyPunishment(state: GameScreenState, currentPlayer: Player?) =
        handlers.random().applyPunishment(state, currentPlayer)

    override fun applyReward(state: GameScreenState) =
        handlers.random().applyReward(state)

    override fun applyMiniGameResult(state: GameScreenState) =
        handlers.random().applyMiniGameResult(state)

    override fun clearEvent(state: GameScreenState) = state.copy(
        barMode = state.barMode.copy(activeEvent = null, deck = emptyList()),
        couplesMode = state.couplesMode.copy(activeEvent = null, deck = emptyList())
    )
}
