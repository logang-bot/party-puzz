package com.restrusher.partypuzl.ui.views.customPacks.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.data.models.CustomPackDraft
import com.restrusher.partypuzl.data.models.PACK_DESCRIPTION_MAX
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomPackViewModel @Inject constructor(
    private val customPackRepository: CustomPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomPackState())
    val uiState: StateFlow<CreateCustomPackState> = _uiState.asStateFlow()

    /**
     * Fills the form when the screen was opened to edit an existing pack. Guarded so returning to
     * the screen doesn't throw away whatever the user has typed since.
     */
    fun load(packId: String?) {
        if (packId == null || uiState.value.packId != null) return
        viewModelScope.launch {
            val summary = customPackRepository.getSummaryOnce(packId) ?: return@launch
            _uiState.update {
                it.copy(
                    packId = summary.packId,
                    name = summary.name,
                    category = summary.category,
                    spice = summary.spice,
                    description = summary.description
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onCategoryChange(value: PackCategory) = _uiState.update { it.copy(category = value) }

    fun onSpiceChange(value: SpiceLevel) = _uiState.update { it.copy(spice = value) }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(PACK_DESCRIPTION_MAX))
    }

    /** Hands the id back so a freshly created pack can open straight into its editor. */
    fun onSave(onSaved: (String) -> Unit) {
        val state = uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            val id = customPackRepository.savePack(
                CustomPackDraft(
                    id = state.packId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    category = state.category,
                    spice = state.spice
                )
            )
            onSaved(id)
        }
    }
}
