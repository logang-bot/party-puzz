package com.restrusher.partypuzl.ui.views.customPacks.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.PackWarning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomPackEditorViewModel @Inject constructor(
    private val customPackRepository: CustomPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomPackEditorState())
    val uiState: StateFlow<CustomPackEditorState> = _uiState.asStateFlow()

    private var loadedPackId: String? = null

    /**
     * Header and entries are collected together so an entry added or removed refreshes the count
     * and the playability warning in the same frame.
     */
    fun load(packId: String) {
        if (loadedPackId == packId) return
        loadedPackId = packId
        viewModelScope.launch {
            combine(
                customPackRepository.getSummary(packId),
                customPackRepository.getEntries(packId)
            ) { summary, entries -> summary to entries }
                .collect { (summary, entries) ->
                    _uiState.update {
                        it.copy(pack = summary?.toUiModel(entries), entries = entries)
                    }
                }
        }
    }

    fun onDeleteRequested(entry: CustomEntryEntity) {
        _uiState.update { it.copy(deleteTarget = entry) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(deleteTarget = null) }
    }

    fun onDeleteConfirmed() {
        val target = uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            customPackRepository.deleteEntry(target.id)
            _uiState.update { it.copy(deleteTarget = null) }
        }
    }
}

/**
 * The warning is derived from the entries flow rather than the summary's stored counts so it
 * updates the instant an entry is added, without waiting for the summary query to re-run.
 */
private fun CustomPackSummary.toUiModel(entries: List<CustomEntryEntity>) = CustomPackUiModel(
    id = packId,
    name = name,
    description = description,
    topic = topic,
    spice = spice,
    entryCount = entries.count { it.isEnabled },
    isAvailable = isAvailable,
    warning = entries.warning()
)

private fun List<CustomEntryEntity>.warning(): PackWarning? {
    val playable = filter { it.isEnabled }
    val truths = playable.count { it.type == CustomEntryType.TRUTH }
    val dares = playable.count { it.type == CustomEntryType.DARE }
    return when {
        playable.isEmpty() -> PackWarning.EMPTY
        truths > 0 && dares == 0 -> PackWarning.TRUTHS_ONLY
        dares > 0 && truths == 0 -> PackWarning.DARES_ONLY
        else -> null
    }
}
