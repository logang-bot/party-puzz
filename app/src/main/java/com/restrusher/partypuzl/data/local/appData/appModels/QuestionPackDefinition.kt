package com.restrusher.partypuzl.data.local.appData.appModels

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier

/**
 * A built-in pack as declared in `QuestionPackCatalog` — the half of a pack that ships with the
 * app and never changes at runtime. The mutable half (enabled / unlocked) lives in Room, and the
 * pack's questions are listed in `QuestionCatalog`.
 *
 * [accent] is the pack's colour on the setup screen. Packs are colour-coded per the design
 * rather than themed, so these are literal values rather than `colorScheme` roles.
 */
data class QuestionPackDefinition(
    val id: String,
    val tier: PackTier,
    val category: PackCategory,
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    val accent: Color
)
