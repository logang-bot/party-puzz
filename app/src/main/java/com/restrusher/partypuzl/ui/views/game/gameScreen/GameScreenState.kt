package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.Player

internal const val SURPRISE_SHUFFLE_DURATION_MS = 1600L
internal const val OUTCOME_SPIN_DURATION_MS = 1800L

enum class GameDealPhase { DEAL_CHOICE, SURPRISE_SHUFFLE, CHALLENGE_SHOWN }

enum class GameDealType { TRUTH_OR_DARE, STICKY_DARE, GENERAL_KNOWLEDGE, MINI_GAME }

enum class OutcomeStage { SPINNING, REVEALED }

enum class TruthOrDareChoice { TRUTH, DARE }

data class GeneralKnowledgeQuestion(
    val question: String,
    val optionA: String,
    val optionB: String,
    val correctOption: Char
)

sealed interface MiniGameResult

data class ScoredMiniGameResult(
    val player1Name: String,
    val player1Score: Int,
    val player2Name: String,
    val player2Score: Int,
    val showScoreDetails: Boolean = true
) : MiniGameResult {
    val winner: String?
        get() = when {
            player1Score > player2Score -> player1Name
            player2Score > player1Score -> player2Name
            else -> null
        }
}

data class LoserMiniGameResult(
    val loserName: String
) : MiniGameResult

data class GameScreenState(
    val players: List<Player> = emptyList(),
    val dealPhase: GameDealPhase = GameDealPhase.DEAL_CHOICE,
    val roundNumber: Int = 0,
    val selectedPlayer: Player? = null,
    // Current deal
    val dealType: GameDealType? = null,
    val challengeText: String? = null,
    // Deal picker — the last category played party-wide is promoted to the hero card
    val heroDealType: GameDealType = GameDealType.TRUTH_OR_DARE,
    val surpriseDealType: GameDealType? = null,
    // Truth or dare
    val truthOrDareChoice: TruthOrDareChoice? = null,
    // General knowledge
    val generalKnowledgeQuestion: GeneralKnowledgeQuestion? = null,
    val selectedAnswerOption: Char? = null,
    // Sticky dare metadata (set alongside challengeText when dealType = STICKY_DARE)
    val stickyDarePresentContinuous: String? = null,
    val stickyDareDurationLabel: String? = null,
    val stickyDareDurationSeconds: Int? = null,
    // Active sticky dare timers
    val activeStickyDares: List<ActiveStickyDare> = emptyList(),
    // Mini-game
    val miniGame: MiniGame? = null,
    val miniGameOpponent: Player? = null,
    val miniGameResult: MiniGameResult? = null,
    // Bar mode
    val barMode: BarModeState = BarModeState(),
    // Couples mode
    val couplesMode: CouplesModeState = CouplesModeState(),
    // Reward / punishment reveal — spins before it lands
    val outcomeStage: OutcomeStage? = null,
    // Camera request — decided at CHALLENGE_SHOWN, consumed after dare or mode event dismissal
    val pendingCameraRequest: Boolean = false,
    val showCameraRequest: Boolean = false
) {
    val isModeActive: Boolean
        get() = barMode.isActive || couplesMode.isActive

    val hasActiveModeEvent: Boolean
        get() = barMode.activeEvent != null || couplesMode.activeEvent != null

    val activeEventCategory: EventCategory?
        get() = couplesMode.activeEvent?.category ?: barMode.activeEvent?.category

    val availableDealTypes: List<GameDealType>
        get() = GameDealType.entries.filter {
            it != GameDealType.MINI_GAME || players.size >= MINI_GAME_MIN_PLAYERS
        }

    val resolvedHeroDealType: GameDealType
        get() = heroDealType.takeIf { it in availableDealTypes } ?: GameDealType.TRUTH_OR_DARE

    val compactDealTypes: List<GameDealType>
        get() = availableDealTypes - resolvedHeroDealType

    val isChallengeDismissible: Boolean
        get() = when (dealType) {
            GameDealType.TRUTH_OR_DARE -> truthOrDareChoice != null
            GameDealType.STICKY_DARE -> true
            GameDealType.GENERAL_KNOWLEDGE -> selectedAnswerOption != null
            GameDealType.MINI_GAME -> miniGameResult != null
            null -> false
        }

    private companion object {
        const val MINI_GAME_MIN_PLAYERS = 2
    }
}
