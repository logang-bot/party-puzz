package com.restrusher.partypuzz.ui.views.game.miniGames.simonSays

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimonSaysViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_START = 3
        private const val BUTTON_HIGHLIGHT_MS = 600L
        private const val BUTTON_GAP_MS = 200L
    }

    private val _uiState = MutableStateFlow(
        SimonSaysState(
            players = GamePlayersList.PlayersList.toList(),
            phase = SimonSaysPhase.COUNTDOWN,
            countdownValue = COUNTDOWN_START
        )
    )
    val uiState: StateFlow<SimonSaysState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var showJob: Job? = null

    init { launchCountdown() }

    fun onButtonTapped(buttonIndex: Int) {
        if (_uiState.value.phase != SimonSaysPhase.INPUT) return
        val expected = _uiState.value.sequence[_uiState.value.playerInputIndex]
        if (buttonIndex != expected) { handleMistake(); return }
        handleCorrectTap()
    }

    fun onPassConfirmed() = startNextRound()

    private fun handleMistake() {
        _uiState.update { it.copy(phase = SimonSaysPhase.GAME_OVER, loser = it.currentPlayer) }
    }

    private fun handleCorrectTap() {
        _uiState.update { state ->
            val next = state.playerInputIndex + 1
            if (next < state.sequence.size) return@update state.copy(playerInputIndex = next)
            val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
            state.copy(playerInputIndex = next, phase = SimonSaysPhase.PASS, currentPlayerIndex = nextPlayerIndex)
        }
    }

    private fun launchCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in COUNTDOWN_START downTo 1) {
                _uiState.update { it.copy(countdownValue = i) }
                delay(1000L)
            }
            _uiState.update { it.copy(countdownValue = 0) }
            delay(1000L)
            startNextRound()
        }
    }

    private fun startNextRound() {
        val newSequence = _uiState.value.sequence + (0..3).random()
        _uiState.update {
            it.copy(sequence = newSequence, playerInputIndex = 0, phase = SimonSaysPhase.SHOWING, highlightedButton = -1)
        }
        showSequence(newSequence)
    }

    private fun showSequence(sequence: List<Int>) {
        showJob?.cancel()
        showJob = viewModelScope.launch {
            delay(400L)
            for (button in sequence) {
                _uiState.update { it.copy(highlightedButton = button) }
                delay(BUTTON_HIGHLIGHT_MS)
                _uiState.update { it.copy(highlightedButton = -1) }
                delay(BUTTON_GAP_MS)
            }
            _uiState.update { it.copy(phase = SimonSaysPhase.INPUT) }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        showJob?.cancel()
        super.onCleared()
    }
}
