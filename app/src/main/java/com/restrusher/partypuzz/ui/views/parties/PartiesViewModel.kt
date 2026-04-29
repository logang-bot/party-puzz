package com.restrusher.partypuzz.ui.views.parties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartiesViewModel @Inject constructor(
    private val partyRepository: PartyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartiesState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            partyRepository.getAllParties().collect { parties ->
                _uiState.update {
                    it.copy(
                        parties = parties,
                        isLoading = false,
                        totalPhotoCount = parties.sumOf { p -> p.photos.size }
                    )
                }
            }
        }
    }
}
