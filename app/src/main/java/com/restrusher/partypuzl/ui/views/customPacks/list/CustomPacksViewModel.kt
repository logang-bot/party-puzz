package com.restrusher.partypuzl.ui.views.customPacks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomPacksViewModel @Inject constructor(
    private val customPackRepository: CustomPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomPacksState())
    val uiState: StateFlow<CustomPacksState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customPackRepository.getSummaries().collect { summaries ->
                _uiState.update {
                    it.copy(isLoading = false, packs = summaries.map(CustomPackSummary::toUiModel))
                }
            }
        }
    }

    fun onToggle(packId: String) {
        val pack = uiState.value.packs.firstOrNull { it.id == packId } ?: return
        viewModelScope.launch { customPackRepository.setAvailable(packId, !pack.isAvailable) }
    }

    fun onDeleteRequested(pack: CustomPackUiModel) {
        _uiState.update { it.copy(deleteTarget = pack) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(deleteTarget = null) }
    }

    fun onDeleteConfirmed() {
        val target = uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            customPackRepository.deletePack(target.id)
            _uiState.update { it.copy(deleteTarget = null) }
        }
    }
}

private fun CustomPackSummary.toUiModel() = CustomPackUiModel(
    id = packId,
    name = name,
    description = description,
    topic = topic,
    spice = spice,
    entryCount = entryCount,
    isAvailable = isAvailable,
    warning = warning()
)

/**
 * Truth or Dare needs both halves — the reveal screen offers a Truth card and a Dare card with no
 * way to hide one — so a pack with only one half can't carry that category on its own. Reported,
 * never blocked: paired with another pack it plays fine.
 */
private fun CustomPackSummary.warning(): PackWarning? = when {
    entryCount == 0 -> PackWarning.EMPTY
    truthCount > 0 && dareCount == 0 -> PackWarning.TRUTHS_ONLY
    dareCount > 0 && truthCount == 0 -> PackWarning.DARES_ONLY
    else -> null
}
