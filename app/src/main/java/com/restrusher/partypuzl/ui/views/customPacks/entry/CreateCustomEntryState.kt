package com.restrusher.partypuzl.ui.views.customPacks.entry

import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.DEFAULT_STICKY_DURATION_SECONDS
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import com.restrusher.partypuzl.ui.views.customPacks.model.deal

data class CreateCustomEntryState(
    val packId: String = "",
    val packName: String = "",
    val entryId: String? = null,
    val type: CustomEntryType = CustomEntryType.TRUTH,
    val text: String = "",
    val durationSeconds: Int = DEFAULT_STICKY_DURATION_SECONDS,
    val optionA: String = "",
    val optionB: String = "",
    /** `'A'` or `'B'` — which of the two options is right. */
    val correctOption: Char = 'A'
) {
    val isEditing: Boolean get() = entryId != null

    /** Which card step 01 shows as picked. A truth and a dare share one. */
    val deal: EntryDeal get() = type.deal

    /**
     * Trivia needs a question and both answers. Everything else needs its text — and for a sticky
     * dare that text also becomes the ticker phrasing and the dedup key, so blank is never valid.
     */
    val canSave: Boolean
        get() = text.isNotBlank() && when (type) {
            CustomEntryType.TRIVIA -> optionA.isNotBlank() && optionB.isNotBlank()
            else -> true
        }
}
