package com.restrusher.partypuzl.ui.views.customPacks.editor

import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.domain.customPacks.CustomPackQuota
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel

data class CustomPackEditorState(
    val pack: CustomPackUiModel? = null,
    val entries: List<CustomEntryEntity> = emptyList(),
    /** Non-null while the delete confirmation is up, holding the entry about to go. */
    val deleteTarget: CustomEntryEntity? = null,
    /** Null until loaded; adding is allowed meanwhile and re-checked when the entry is saved. */
    val quota: CustomPackQuota? = null,
    val isUpsellOpen: Boolean = false
) {
    val canAddEntry: Boolean get() = quota?.canAddEntry(entries.size) ?: true
}
