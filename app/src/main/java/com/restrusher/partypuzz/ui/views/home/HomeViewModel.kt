package com.restrusher.partypuzz.ui.views.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameModesDatasource
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val partyRepository: PartyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState(gameModes = GameModesDatasource.gameModesList))
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            partyRepository.getAllParties().collect { parties ->
                _uiState.update { it.copy(allParties = parties, isLoading = false) }
            }
        }
    }

    fun togglePartySelection() =
        _uiState.update { it.copy(isPartySelected = !it.isPartySelected) }

    fun openDialog() = _uiState.update {
        it.copy(
            isDialogOpen = true,
            dialogPendingPartyId = if (it.isPartySelected) it.activeParty?.party?.id else null
        )
    }

    fun closeDialog() =
        _uiState.update { it.copy(isDialogOpen = false, dialogPendingPartyId = null) }

    fun selectDialogParty(partyId: Int) =
        _uiState.update { it.copy(dialogPendingPartyId = partyId) }

    fun confirmPartySelection() {
        val partyId = _uiState.value.dialogPendingPartyId ?: return
        viewModelScope.launch {
            partyRepository.updateLastUsed(partyId)
            _uiState.update { it.copy(isDialogOpen = false, dialogPendingPartyId = null, isPartySelected = true, isPartyCustomSelected = true) }
        }
    }
}
