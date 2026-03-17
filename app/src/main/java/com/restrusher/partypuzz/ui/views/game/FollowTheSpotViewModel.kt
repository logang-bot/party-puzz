package com.restrusher.partypuzz.ui.views.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class FollowTheSpotViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val GAME_DURATION_SECONDS = 15
    }

    private val _uiState = MutableStateFlow(FollowTheSpotState())
    val uiState: StateFlow<FollowTheSpotState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startGame()
    }

    fun onPlayer1SpotTapped() {
        if (!_uiState.value.isGameRunning) return
        _uiState.update {
            it.copy(
                player1Score = it.player1Score + 1,
                player1SpotNormX = Random.nextFloat(),
                player1SpotNormY = Random.nextFloat()
            )
        }
    }

    fun onPlayer2SpotTapped() {
        if (!_uiState.value.isGameRunning) return
        _uiState.update {
            it.copy(
                player2Score = it.player2Score + 1,
                player2SpotNormX = Random.nextFloat(),
                player2SpotNormY = Random.nextFloat()
            )
        }
    }

    private fun startGame() {
        _uiState.value = FollowTheSpotState(
            player1SpotNormX = Random.nextFloat(),
            player1SpotNormY = Random.nextFloat(),
            player2SpotNormX = Random.nextFloat(),
            player2SpotNormY = Random.nextFloat(),
            isGameRunning = true
        )
        launchTimer()
    }

    private fun launchTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = GAME_DURATION_SECONDS
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(timeRemaining = remaining) }
            }
            _uiState.update { it.copy(isGameRunning = false) }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
