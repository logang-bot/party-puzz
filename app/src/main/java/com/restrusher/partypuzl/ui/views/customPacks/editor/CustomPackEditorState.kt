package com.restrusher.partypuzl.ui.views.customPacks.editor

import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel

data class CustomPackEditorState(
    val pack: CustomPackUiModel? = null,
    val entries: List<CustomEntryEntity> = emptyList(),
    /** Non-null while the delete confirmation is up, holding the entry about to go. */
    val deleteTarget: CustomEntryEntity? = null
)
