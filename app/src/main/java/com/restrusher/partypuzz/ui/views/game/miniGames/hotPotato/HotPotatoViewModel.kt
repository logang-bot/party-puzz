package com.restrusher.partypuzz.ui.views.game.miniGames.hotPotato

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
import kotlin.random.Random

@HiltViewModel
class HotPotatoViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_START = 3
        private const val MIN_GAME_SECONDS = 10
        private const val MAX_GAME_SECONDS = 30
    }

    private val players = GamePlayersList.PlayersList.toList()

    private val _uiState = MutableStateFlow(
        HotPotatoState(
            players = players,
            currentHolderIndex = if (players.isNotEmpty()) Random.nextInt(players.size) else 0,
            isCountingDown = true,
            countdownValue = COUNTDOWN_START
        )
    )
    val uiState: StateFlow<HotPotatoState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var timerJob: Job? = null

    init {
        launchCountdown()
    }

    fun onPassTapped() {
        val state = _uiState.value
        if (!state.isGameRunning || state.players.size < 2) return
        val next = (state.currentHolderIndex + 1) % state.players.size
        _uiState.update { it.copy(currentHolderIndex = next) }
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
            startGame()
        }
    }

    private fun startGame() {
        val duration = Random.nextInt(MIN_GAME_SECONDS, MAX_GAME_SECONDS + 1)
        _uiState.update { it.copy(isGameRunning = true, isCountingDown = false) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            delay(duration * 1000L)
            _uiState.update { it.copy(isGameRunning = false, loserIndex = it.currentHolderIndex) }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        timerJob?.cancel()
        super.onCleared()
    }
}
