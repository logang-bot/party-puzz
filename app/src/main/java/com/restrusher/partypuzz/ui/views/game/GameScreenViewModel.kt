package com.restrusher.partypuzz.ui.views.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val ANIMATION_DURATION_MS = 5000L
        private const val NAME_CYCLE_INTERVAL_MS = 300L
        private const val REVEAL_DURATION_MS = 1000L
    }

    private val _uiState = MutableStateFlow(
        GameScreenState(players = GamePlayersList.PlayersList.toList())
    )
    val uiState: StateFlow<GameScreenState> = _uiState.asStateFlow()

    private var dealJob: Job? = null

    fun onGameDealTapped() {
        val state = _uiState.value
        if (state.dealPhase != GameDealPhase.IDLE || state.players.isEmpty()) return

        val selectedPlayer = state.players.random()
        val dealType = GameDealType.entries.random()

        dealJob?.cancel()
        dealJob = viewModelScope.launch {
            // Phase 1: Cycle through player names for 5 seconds
            _uiState.update { it.copy(dealPhase = GameDealPhase.ANIMATING) }
            val players = _uiState.value.players
            val iterations = (ANIMATION_DURATION_MS / NAME_CYCLE_INTERVAL_MS).toInt()
            val namePool = players.map { it.nickName }.toMutableList()
            repeat(iterations) { i ->
                if (i % players.size == 0) namePool.shuffle()
                _uiState.update { it.copy(animatingName = namePool[i % players.size]) }
                delay(NAME_CYCLE_INTERVAL_MS)
            }

            // Phase 2: Reveal selected player's name
            _uiState.update {
                it.copy(
                    dealPhase = GameDealPhase.PLAYER_NAME_REVEAL,
                    selectedPlayer = selectedPlayer,
                    animatingName = ""
                )
            }
            delay(REVEAL_DURATION_MS)

            // Phase 3: Reveal selected player's photo
            _uiState.update { it.copy(dealPhase = GameDealPhase.PLAYER_PHOTO_REVEAL) }
            delay(REVEAL_DURATION_MS)

            // Phase 4: Show challenge — content depends on deal type
            val challengeText = if (dealType == GameDealType.STICKY_DARE) {
                context.resources.getStringArray(R.array.sticky_dares).random()
            } else null

            val gkQuestion = if (dealType == GameDealType.GENERAL_KNOWLEDGE) {
                loadGkQuestions().randomOrNull()
            } else null

            _uiState.update {
                it.copy(
                    dealPhase = GameDealPhase.CHALLENGE_SHOWN,
                    dealType = dealType,
                    challengeText = challengeText,
                    generalKnowledgeQuestion = gkQuestion
                )
            }
        }
    }

    fun onTruthOrDareChosen(choice: TruthOrDareChoice) {
        if (_uiState.value.truthOrDareChoice != null) return
        val texts = when (choice) {
            TruthOrDareChoice.TRUTH -> context.resources.getStringArray(R.array.truth_texts)
            TruthOrDareChoice.DARE -> context.resources.getStringArray(R.array.dare_texts)
        }
        _uiState.update {
            it.copy(truthOrDareChoice = choice, challengeText = texts.random())
        }
    }

    fun onGeneralKnowledgeAnswered(option: Char) {
        if (_uiState.value.selectedAnswerOption != null) return
        _uiState.update { it.copy(selectedAnswerOption = option) }
    }

    fun onChallengeDismissed() {
        if (!_uiState.value.isChallengeDismissible) return
        dealJob?.cancel()
        _uiState.update {
            it.copy(
                dealPhase = GameDealPhase.IDLE,
                selectedPlayer = null,
                animatingName = "",
                dealType = null,
                challengeText = null,
                truthOrDareChoice = null,
                generalKnowledgeQuestion = null,
                selectedAnswerOption = null
            )
        }
    }

    private fun loadGkQuestions(): List<GeneralKnowledgeQuestion> {
        val questions = context.resources.getStringArray(R.array.gk_questions)
        val optionsA = context.resources.getStringArray(R.array.gk_options_a)
        val optionsB = context.resources.getStringArray(R.array.gk_options_b)
        val correctOptions = context.resources.getStringArray(R.array.gk_correct_options)
        return questions.indices.map { i ->
            GeneralKnowledgeQuestion(
                question = questions[i],
                optionA = optionsA[i],
                optionB = optionsB[i],
                correctOption = correctOptions[i].first()
            )
        }
    }

    override fun onCleared() {
        dealJob?.cancel()
        super.onCleared()
    }
}
