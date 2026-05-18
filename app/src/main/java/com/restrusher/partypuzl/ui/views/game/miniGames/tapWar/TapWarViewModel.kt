package com.restrusher.partypuzl.ui.views.game.miniGames.tapWar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.navigation.TapWarRoute
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
class TapWarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val BAR_STEP = 0.04f
        private const val COUNTDOWN_START = 3
        const val GAME_DURATION_SECONDS = 10
    }

    private val _uiState = MutableStateFlow(TapWarState())
    val uiState: StateFlow<TapWarState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var timerJob: Job? = null

    init {
        val route = savedStateHandle.toRoute<TapWarRoute>()
        val player1 = Player(
            id = 0, nickName = route.player1Name, gender = Gender.Unknown,
            interestedIn = InterestedIn.Man, photoPath = route.player1PhotoPath,
            avatarName = route.player1AvatarName
        )
        val player2 = Player(
            id = 1, nickName = route.player2Name, gender = Gender.Unknown,
            interestedIn = InterestedIn.Man, photoPath = route.player2PhotoPath,
            avatarName = route.player2AvatarName
        )
        _uiState.value = TapWarState(
            player1 = player1, player2 = player2,
            isCountingDown = true, countdownValue = COUNTDOWN_START
        )
        launchCountdown(player1, player2)
    }

    fun onPlayer1Tapped() = applyTap(BAR_STEP)

    fun onPlayer2Tapped() = applyTap(-BAR_STEP)

    private fun applyTap(delta: Float) {
        if (!_uiState.value.isGameRunning) return
        val newPos = (_uiState.value.barPosition + delta).coerceIn(0f, 1f)
        val p1Won = newPos >= 1f
        val p2Won = newPos <= 0f
        if (p1Won || p2Won) timerJob?.cancel()
        _uiState.update {
            it.copy(
                barPosition = newPos,
                isGameRunning = !p1Won && !p2Won,
                winner = when { p1Won -> 1; p2Won -> 2; else -> null }
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
            _uiState.update { it.copy(countdownValue = 0) }
            delay(1000L)
            startGame(player1, player2)
        }
    }

    private fun startGame(player1: Player, player2: Player) {
        _uiState.update {
            it.copy(
                player1 = player1, player2 = player2,
                barPosition = 0.5f, isGameRunning = true,
                isCountingDown = false, timeRemaining = GAME_DURATION_SECONDS
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
            endGameByTime()
        }
    }

    private fun endGameByTime() {
        _uiState.update { state ->
            val winner = when {
                state.barPosition > 0.5f -> 1
                state.barPosition < 0.5f -> 2
                else -> null
            }
            state.copy(isGameRunning = false, winner = winner)
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        timerJob?.cancel()
        super.onCleared()
    }
}
