package com.restrusher.partypuzz.ui.views.gameConfig

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzz.navigation.GameConfigScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(
    private val partyRepository: PartyRepository,
    private val playerRepository: PlayerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<GameConfigScreen>()

    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

    init {
        GamePlayersList.resetPlayersList()
        val partyId = args.partyId
        if (partyId != null) {
            GamePlayersList.currentPartyId = partyId
            viewModelScope.launch {
                partyRepository.getPartyById(partyId)?.players?.forEach { entity ->
                    GamePlayersList.addPlayer(
                        Player(
                            id = entity.id,
                            nickName = entity.nickName,
                            gender = entity.gender,
                            photoPath = entity.photoPath,
                            avatarName = entity.avatarName
                        )
                    )
                }
            }
        }
    }

    fun onStartGame(onReady: () -> Unit) {
        viewModelScope.launch {
            val partyId = args.partyId
            if (partyId != null) partyRepository.updateLastUsed(partyId)
            onReady()
        }
    }

    fun deletePlayer(player: Player) {
        GamePlayersList.removePlayer(player.id)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                playerRepository.deletePlayer(
                    PlayerEntity(
                        id = player.id,
                        nickName = player.nickName,
                        gender = player.gender,
                        photoPath = player.photoPath,
                        avatarName = player.avatarName
                    )
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
