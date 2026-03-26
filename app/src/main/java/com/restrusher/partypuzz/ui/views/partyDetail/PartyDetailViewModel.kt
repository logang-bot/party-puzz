package com.restrusher.partypuzz.ui.views.partyDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyDetailViewModel @Inject constructor(
    private val partyRepository: PartyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyDetailState())
    val uiState: StateFlow<PartyDetailState> = _uiState.asStateFlow()

    fun loadParty(partyId: Int) {
        if (_uiState.value.party != null) return
        viewModelScope.launch {
            val party = partyRepository.getPartyById(partyId)
            _uiState.update { it.copy(party = party, isLoading = false) }
        }
    }

    fun startEditing() {
        val name = _uiState.value.party?.party?.name ?: return
        _uiState.update { it.copy(isEditing = true, editedName = name) }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(editedName = name) }

    fun discardEditing() = _uiState.update { it.copy(isEditing = false, editedName = "") }

    fun savePartyName() {
        val partyId = _uiState.value.party?.party?.id ?: return
        val newName = _uiState.value.editedName.trim()
        if (newName.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            partyRepository.updatePartyName(partyId, newName)
            val updated = partyRepository.getPartyById(partyId)
            _uiState.update { it.copy(party = updated, isSaving = false, isEditing = false, editedName = "") }
        }
    }

    fun showDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = true) }

    fun dismissDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        val partyId = _uiState.value.party?.party?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }
            partyRepository.deleteParty(partyId)
            _uiState.update { it.copy(isDeleting = false, navigateBack = true) }
        }
    }
}
