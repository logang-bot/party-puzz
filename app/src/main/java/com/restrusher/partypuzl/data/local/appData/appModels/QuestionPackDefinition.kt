package com.restrusher.partypuzl.data.local.appData.appModels

import androidx.annotation.StringRes
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.SpiceLevel

/**
 * A built-in pack as declared in `QuestionPackCatalog` — the half of a pack that ships with the
 * app and never changes at runtime. The mutable half (enabled / unlocked) lives in Room, and the
 * pack's questions are listed in `QuestionCatalog`.
 *
 * [spice] is the pack's whole look. It is declared rather than an icon and an accent so a
 * built-in pack cannot end up wearing something the create-pack screen has no way to produce:
 * `SpiceLevel.iconRes` and `SpiceLevel.packAccent` resolve it, and authored packs go through the
 * same two.
 */
data class QuestionPackDefinition(
    val id: String,
    val tier: PackTier,
    val category: PackCategory,
    @StringRes val nameRes: Int,
    val spice: SpiceLevel
)
