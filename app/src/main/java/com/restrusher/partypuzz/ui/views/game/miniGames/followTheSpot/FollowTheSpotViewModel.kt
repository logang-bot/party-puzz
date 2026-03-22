package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.navigation.FollowTheSpotRoute
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
class FollowTheSpotViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val GAME_DURATION_SECONDS = 15
        private const val COUNTDOWN_START = 3
    }

    private val _uiState = MutableStateFlow(FollowTheSpotState())
    val uiState: StateFlow<FollowTheSpotState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var timerJob: Job? = null

    init {
        val route = savedStateHandle.toRoute<FollowTheSpotRoute>()
        val player1 = Player(
            id = 0,
            nickName = route.player1Name,
            gender = Gender.Unknown,
            photoPath = route.player1PhotoPath,
            avatarName = route.player1AvatarName
        )
        val player2 = Player(
            id = 1,
            nickName = route.player2Name,
            gender = Gender.Unknown,
            photoPath = route.player2PhotoPath,
            avatarName = route.player2AvatarName
        )
        _uiState.value = FollowTheSpotState(
            player1 = player1,
            player2 = player2,
            isCountingDown = true,
            countdownValue = COUNTDOWN_START
        )
        launchCountdown(player1, player2)
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

    private fun launchCountdown(player1: Player, player2: Player) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in COUNTDOWN_START downTo 1) {
                _uiState.update { it.copy(countdownValue = i) }
                delay(1000L)
            }
            // Show "Go!" and freeze for 1 second before starting
            _uiState.update { it.copy(countdownValue = 0) }
            delay(1000L)
            startGame(player1, player2)
        }
    }

    private fun startGame(player1: Player, player2: Player) {
        _uiState.update {
            it.copy(
                player1 = player1,
                player2 = player2,
                player1Score = 0,
                player2Score = 0,
                player1SpotNormX = Random.nextFloat(),
                player1SpotNormY = Random.nextFloat(),
                player2SpotNormX = Random.nextFloat(),
                player2SpotNormY = Random.nextFloat(),
                timeRemaining = GAME_DURATION_SECONDS,
                isGameRunning = true,
                isCountingDown = false
            )
        }
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
        countdownJob?.cancel()
        timerJob?.cancel()
        super.onCleared()
    }
}
