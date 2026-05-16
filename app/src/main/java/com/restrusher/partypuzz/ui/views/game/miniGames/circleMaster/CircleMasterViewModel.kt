package com.restrusher.partypuzz.ui.views.game.miniGames.circleMaster

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class CircleMasterViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_START = 3
        private const val MIN_POINTS = 20
    }

    private val _uiState = MutableStateFlow(
        CircleMasterState(players = GamePlayersList.PlayersList.toList())
    )
    val uiState: StateFlow<CircleMasterState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init { launchCountdown() }

    fun onDrawPoint(offset: Offset) {
        if (_uiState.value.phase != CircleMasterPhase.DRAWING) return
        _uiState.update { it.copy(drawnPath = it.drawnPath + offset) }
    }

    fun onDrawEnd() {
        val state = _uiState.value
        if (state.phase != CircleMasterPhase.DRAWING) return
        val playerName = state.currentPlayer?.nickName ?: return
        val score = computeScore(state.drawnPath)
        _uiState.update {
            it.copy(
                currentScore = score,
                scores = it.scores + (playerName to score),
                phase = CircleMasterPhase.SCORE_REVEAL
            )
        }
    }

    fun onScoreAcknowledged() {
        val state = _uiState.value
        if (state.phase != CircleMasterPhase.SCORE_REVEAL) return
        if (state.isLastPlayer) {
            val (winner, loser) = resolveWinnerLoser(state.players, state.scores)
            _uiState.update { it.copy(phase = CircleMasterPhase.WINNER, winner = winner, loser = loser) }
        } else {
            _uiState.update { it.copy(phase = CircleMasterPhase.PASS) }
        }
    }

    fun onPassConfirmed() {
        if (_uiState.value.phase != CircleMasterPhase.PASS) return
        _uiState.update {
            it.copy(
                currentPlayerIndex = it.currentPlayerIndex + 1,
                phase = CircleMasterPhase.DRAWING,
                drawnPath = emptyList(),
                currentScore = null
            )
        }
    }

    private fun launchCountdown() {
        countdownJob = viewModelScope.launch {
            for (i in COUNTDOWN_START downTo 1) {
                _uiState.update { it.copy(countdownValue = i) }
                delay(1000L)
            }
            _uiState.update { it.copy(countdownValue = 0) }
            delay(700L)
            _uiState.update { it.copy(phase = CircleMasterPhase.DRAWING) }
        }
    }

    private fun computeScore(path: List<Offset>): Int {
        if (path.size < MIN_POINTS) return 0
        val cx = path.map { it.x }.average().toFloat()
        val cy = path.map { it.y }.average().toFloat()
        val radii = path.map { sqrt((it.x - cx) * (it.x - cx) + (it.y - cy) * (it.y - cy)) }
        val avg = radii.average()
        if (avg < 10.0) return 0
        val variance = radii.map { r -> (r - avg) * (r - avg) }.average()
        return ((1.0 - (sqrt(variance) / avg).coerceIn(0.0, 1.0)) * 100).toInt().coerceIn(0, 100)
    }

    private fun resolveWinnerLoser(players: List<Player>, scores: Map<String, Int>): Pair<Player?, Player?> {
        val sorted = players.sortedByDescending { scores[it.nickName] ?: 0 }
        return sorted.firstOrNull() to sorted.lastOrNull()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}
