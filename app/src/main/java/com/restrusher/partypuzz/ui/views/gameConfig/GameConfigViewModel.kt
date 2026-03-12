package com.restrusher.partypuzz.ui.views.gameConfig

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzz.navigation.GameConfigScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(
    private val partyRepository: PartyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<GameConfigScreen>()

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
}
