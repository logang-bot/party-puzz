package com.restrusher.partypuzz.ui.views.game

import com.restrusher.partypuzz.data.models.Player

enum class GameDealPhase {
    IDLE, ANIMATING, PLAYER_NAME_REVEAL, PLAYER_PHOTO_REVEAL, CHALLENGE_SHOWN
}

enum class GameDealType { TRUTH_OR_DARE, STICKY_DARE, GENERAL_KNOWLEDGE }

enum class TruthOrDareChoice { TRUTH, DARE }

data class GeneralKnowledgeQuestion(
    val question: String,
    val optionA: String,
    val optionB: String,
    val correctOption: Char
)

data class GameScreenState(
    val players: List<Player> = emptyList(),
    val dealPhase: GameDealPhase = GameDealPhase.IDLE,
    val animatingName: String = "",
    val selectedPlayer: Player? = null,
    val dealType: GameDealType? = null,
    val challengeText: String? = null,
    val truthOrDareChoice: TruthOrDareChoice? = null,
    val generalKnowledgeQuestion: GeneralKnowledgeQuestion? = null,
    val selectedAnswerOption: Char? = null
) {
    val isChallengeDismissible: Boolean
        get() = when (dealType) {
            GameDealType.TRUTH_OR_DARE -> truthOrDareChoice != null
            GameDealType.STICKY_DARE -> true
            GameDealType.GENERAL_KNOWLEDGE -> selectedAnswerOption != null
            null -> false
        }
}
