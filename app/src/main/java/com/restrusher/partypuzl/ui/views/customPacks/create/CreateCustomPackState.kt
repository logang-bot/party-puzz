package com.restrusher.partypuzl.ui.views.customPacks.create

import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel

data class CreateCustomPackState(
    val packId: String? = null,
    val name: String = "",
    val category: PackCategory = PackCategory.TRUTH_OR_DARE,
    val spice: SpiceLevel = SpiceLevel.MEDIUM,
    val description: String = ""
) {
    val isEditing: Boolean get() = packId != null

    /** The description is optional — a pack with a name is enough to start writing entries. */
    val canSave: Boolean get() = name.isNotBlank()
}
