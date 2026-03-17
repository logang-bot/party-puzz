package com.restrusher.partypuzz.ui.views.game

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
class GameScreenViewModel @Inject constructor() : ViewModel() {

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

        dealJob?.cancel()
        dealJob = viewModelScope.launch {
            // Phase 1: Cycle through player names for 5 seconds (visual only — winner already decided)
            _uiState.update { it.copy(dealPhase = GameDealPhase.ANIMATING) }
            val players = _uiState.value.players
            val iterations = (ANIMATION_DURATION_MS / NAME_CYCLE_INTERVAL_MS).toInt()
            val namePool = players.map { it.nickName }.toMutableList()
            repeat(iterations) { i ->
                if (i % players.size == 0) namePool.shuffle()
                _uiState.update { it.copy(animatingName = namePool[i % players.size]) }
                delay(NAME_CYCLE_INTERVAL_MS)
            }

            // Phase 2: Reveal selected player's name for 2 seconds
            _uiState.update {
                it.copy(
                    dealPhase = GameDealPhase.PLAYER_NAME_REVEAL,
                    selectedPlayer = selectedPlayer,
                    animatingName = ""
                )
            }
            delay(REVEAL_DURATION_MS)

            // Phase 3: Reveal selected player's photo for 2 seconds
            _uiState.update { it.copy(dealPhase = GameDealPhase.PLAYER_PHOTO_REVEAL) }
            delay(REVEAL_DURATION_MS)

            // Phase 4: Show challenge card on top
            _uiState.update { it.copy(dealPhase = GameDealPhase.CHALLENGE_SHOWN) }
        }
    }

    fun onChallengeDismissed() {
        dealJob?.cancel()
        _uiState.update {
            it.copy(
                dealPhase = GameDealPhase.IDLE,
                selectedPlayer = null,
                animatingName = ""
            )
        }
    }

    override fun onCleared() {
        dealJob?.cancel()
        super.onCleared()
    }
}
