package com.restrusher.partypuzz.ui.views.createPlayer

import androidx.lifecycle.ViewModel
import com.restrusher.partypuzz.data.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreatePlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreatePlayerState())
    val uiState: StateFlow<CreatePlayerState> = _uiState.asStateFlow()

    fun addPlayer(playerName: String, gender: Gender) {
        val player = Player(GamePlayersList.PlayersList.size, playerName, gender)
        GamePlayersList.addPlayer(player)
        _uiState.update { currentState ->
            currentState.copy(
                playerId = GamePlayersList.PlayersList.size,
                playerName = playerName,
                gender = gender
            )
        }
    }

}