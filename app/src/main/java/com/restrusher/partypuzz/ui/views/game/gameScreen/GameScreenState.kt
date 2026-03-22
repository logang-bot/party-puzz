package com.restrusher.partypuzz.ui.views.game.gameScreen

import com.restrusher.partypuzz.data.models.Player

enum class GameDealPhase {
    IDLE, ANIMATING, PLAYER_NAME_REVEAL, PLAYER_PHOTO_REVEAL, CHALLENGE_SHOWN
}

enum class GameDealType { TRUTH_OR_DARE, STICKY_DARE, GENERAL_KNOWLEDGE, MINI_GAME }

enum class TruthOrDareChoice { TRUTH, DARE }

data class GeneralKnowledgeQuestion(
    val question: String,
    val optionA: String,
    val optionB: String,
    val correctOption: Char
)

data class MiniGameResult(
    val player1Name: String,
    val player1Score: Int,
    val player2Name: String,
    val player2Score: Int
) {
    val winner: String?
        get() = when {
            player1Score > player2Score -> player1Name
            player2Score > player1Score -> player2Name
            else -> null
        }
}

data class GameScreenState(
    val players: List<Player> = emptyList(),
    val dealPhase: GameDealPhase = GameDealPhase.IDLE,
    val animatingName: String = "",
    val selectedPlayer: Player? = null,
    // Current deal
    val dealType: GameDealType? = null,
    val challengeText: String? = null,
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
    val miniGameResult: MiniGameResult? = null
) {
    val isChallengeDismissible: Boolean
        get() = when (dealType) {
            GameDealType.TRUTH_OR_DARE -> truthOrDareChoice != null
            GameDealType.STICKY_DARE -> true
            GameDealType.GENERAL_KNOWLEDGE -> selectedAnswerOption != null
            GameDealType.MINI_GAME -> miniGameResult != null
            null -> false
        }
}
