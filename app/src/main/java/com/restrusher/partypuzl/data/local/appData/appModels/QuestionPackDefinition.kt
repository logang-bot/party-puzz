package com.restrusher.partypuzl.data.local.appData.appModels

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.restrusher.partypuzl.data.models.PackAccent
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier

/**
 * A built-in pack as declared in `QuestionPackCatalog` — the half of a pack that ships with the
 * app and never changes at runtime. The mutable half (enabled / unlocked) lives in Room, and the
 * pack's questions are listed in `QuestionCatalog`.
 *
 * [accent] names the pack's colour-code on the setup screen. Packs are coded per the design
 * rather than themed, so it is a fixed accent rather than a `colorScheme` role — the name is
 * resolved to a colour by `PackAccent.color` in `ui/theme`.
 */
data class QuestionPackDefinition(
    val id: String,
    val tier: PackTier,
    val category: PackCategory,
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    val accent: PackAccent
)
