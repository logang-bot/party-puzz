package com.restrusher.partypuzl.ui.views.customPacks.list

import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel

/**
 * Why a pack won't contribute anything playable yet. Advisory only — nothing is blocked, because
 * a truths-only pack is still perfectly playable next to a pack that has dares.
 */
enum class PackWarning { EMPTY, TRUTHS_ONLY, DARES_ONLY }

data class CustomPackUiModel(
    val id: String,
    val name: String,
    val description: String,
    val category: PackCategory,
    val spice: SpiceLevel,
    val entryCount: Int,
    val isEnabled: Boolean,
    val warning: PackWarning?
)

data class CustomPacksState(
    val isLoading: Boolean = true,
    val packs: List<CustomPackUiModel> = emptyList(),
    /** Non-null while the delete confirmation is up, holding the pack about to go. */
    val deleteTarget: CustomPackUiModel? = null
) {
    val totalEntries: Int get() = packs.sumOf { it.entryCount }
}
