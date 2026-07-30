package com.restrusher.partypuzl.ui.views.customPacks.list

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel

/**
 * Why a pack won't contribute anything playable yet. Advisory only — nothing is blocked, because
 * a truths-only pack is still perfectly playable next to a pack that has dares.
 */
enum class PackWarning { EMPTY, TRUTHS_ONLY, DARES_ONLY }

/** The advisory the pack card and the editor header both show. */
@get:StringRes
internal val PackWarning.messageRes: Int
    get() = when (this) {
        PackWarning.EMPTY -> R.string.custom_pack_warning_empty
        PackWarning.TRUTHS_ONLY -> R.string.custom_pack_warning_truths_only
        PackWarning.DARES_ONLY -> R.string.custom_pack_warning_dares_only
    }

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
