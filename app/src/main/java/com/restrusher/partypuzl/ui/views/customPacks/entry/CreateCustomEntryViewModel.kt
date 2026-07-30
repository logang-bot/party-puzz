package com.restrusher.partypuzl.ui.views.customPacks.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.data.models.CustomEntryDraft
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.DEFAULT_STICKY_DURATION_SECONDS
import com.restrusher.partypuzl.data.models.ENTRY_TEXT_MAX
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.TRIVIA_TEXT_MAX
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import com.restrusher.partypuzl.ui.views.customPacks.model.defaultType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomEntryViewModel @Inject constructor(
    private val customPackRepository: CustomPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomEntryState())
    val uiState: StateFlow<CreateCustomEntryState> = _uiState.asStateFlow()

    private var loaded = false

    /**
     * Reads the pack for the "Adding to …" card, and the entry itself when editing. A new entry
     * starts on the type that matches the pack's declared category — the likeliest pick, though
     * the author is free to change it.
     */
    fun load(packId: String, entryId: String?) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            val summary = customPackRepository.getSummaryOnce(packId)
            val entry = entryId?.let { customPackRepository.getEntry(it) }
            _uiState.update {
                it.copy(
                    packId = packId,
                    packName = summary?.name.orEmpty(),
                    entryId = entry?.id,
                    type = entry?.type ?: defaultTypeFor(summary?.category),
                    text = entry?.text.orEmpty(),
                    durationSeconds = entry?.durationSeconds ?: DEFAULT_STICKY_DURATION_SECONDS,
                    optionA = entry?.optionA.orEmpty(),
                    optionB = entry?.optionB.orEmpty(),
                    correctOption = entry?.correctOption?.firstOrNull() ?: 'A'
                )
            }
        }
    }

    /**
     * Step 01. Re-picking the card the entry is already on is a no-op, so tapping "Truth or Dare"
     * while writing a dare does not silently flip it back to a truth.
     */
    fun onDealChange(value: EntryDeal) = _uiState.update {
        if (value == it.deal) it else it.copy(type = value.defaultType)
    }

    /** Step 02, and only for the truth-or-dare card — which of the two pools the entry joins. */
    fun onKindChange(value: CustomEntryType) = _uiState.update { it.copy(type = value) }

    fun onTextChange(value: String) = _uiState.update {
        val max = if (it.type == CustomEntryType.TRIVIA) TRIVIA_TEXT_MAX else ENTRY_TEXT_MAX
        it.copy(text = value.take(max))
    }

    fun onDurationChange(value: Int) = _uiState.update { it.copy(durationSeconds = value) }

    fun onOptionAChange(value: String) = _uiState.update { it.copy(optionA = value) }

    fun onOptionBChange(value: String) = _uiState.update { it.copy(optionB = value) }

    fun onCorrectOptionChange(value: Char) = _uiState.update { it.copy(correctOption = value) }

    fun onSave(onSaved: () -> Unit) {
        val state = uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            customPackRepository.saveEntry(state.toDraft())
            onSaved()
        }
    }
}

/** Only the fields the picked type actually uses are carried over; the rest stay null. */
private fun CreateCustomEntryState.toDraft() = CustomEntryDraft(
    id = entryId,
    packId = packId,
    type = type,
    text = text.trim(),
    durationSeconds = durationSeconds.takeIf { type == CustomEntryType.STICKY_DARE },
    optionA = optionA.trim().takeIf { type == CustomEntryType.TRIVIA },
    optionB = optionB.trim().takeIf { type == CustomEntryType.TRIVIA },
    correctOption = correctOption.takeIf { type == CustomEntryType.TRIVIA }
)

private fun defaultTypeFor(category: PackCategory?): CustomEntryType = when (category) {
    PackCategory.STICKY_DARE -> CustomEntryType.STICKY_DARE
    PackCategory.GENERAL_KNOWLEDGE -> CustomEntryType.TRIVIA
    else -> CustomEntryType.TRUTH
}
