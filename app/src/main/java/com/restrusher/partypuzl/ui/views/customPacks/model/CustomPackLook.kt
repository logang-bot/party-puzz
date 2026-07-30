package com.restrusher.partypuzl.ui.views.customPacks.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.models.packAccent
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.AccentViolet
import com.restrusher.partypuzl.ui.theme.color

/**
 * How a custom pack and its entries look.
 *
 * Everything colour-related lives here rather than in the data layer, which must not depend on
 * Compose — `SpiceLevel` and `CustomEntryType` stay plain enums and get their looks attached at
 * the UI boundary, the same way `QuestionPackDefinition` carries the built-in packs' accents.
 */

/** Categories a user can actually author for. Mini-games are code, not prompts. */
val AuthorableCategories = listOf(
    PackCategory.TRUTH_OR_DARE,
    PackCategory.STICKY_DARE,
    PackCategory.GENERAL_KNOWLEDGE
)

@get:DrawableRes
val SpiceLevel.iconRes: Int
    get() = when (this) {
        SpiceLevel.MILD -> R.drawable.ic_outline_mood
        SpiceLevel.MEDIUM -> R.drawable.ic_lightbulb
        SpiceLevel.SPICY -> R.drawable.ic_whatshot
    }

val SpiceLevel.accent: Color
    get() = packAccent.color

@get:StringRes
val SpiceLevel.labelRes: Int
    get() = when (this) {
        SpiceLevel.MILD -> R.string.spice_mild
        SpiceLevel.MEDIUM -> R.string.spice_medium
        SpiceLevel.SPICY -> R.string.spice_spicy
    }

@get:StringRes
val PackCategory.labelRes: Int
    get() = when (this) {
        PackCategory.TRUTH_OR_DARE -> R.string.truth_or_dare
        PackCategory.STICKY_DARE -> R.string.sticky_dares
        PackCategory.GENERAL_KNOWLEDGE -> R.string.general_culture
        PackCategory.MINI_GAME -> R.string.mini_games
    }

@get:DrawableRes
val CustomEntryType.iconRes: Int
    get() = when (this) {
        CustomEntryType.TRUTH -> R.drawable.ic_lightbulb
        CustomEntryType.DARE -> R.drawable.ic_whatshot
        CustomEntryType.STICKY_DARE -> R.drawable.ic_hourglass
        CustomEntryType.TRIVIA -> R.drawable.ic_chat_bubble
    }

/**
 * Truths and dares share a tone: they are one deal ([EntryDeal.TRUTH_OR_DARE]) wherever a colour
 * is drawn, and the half is told apart by the label instead. Teal survives as the Truth side of
 * the step-02 toggle, which is the one place the two are being contrasted.
 */
val CustomEntryType.accent: Color
    get() = when (this) {
        CustomEntryType.TRUTH, CustomEntryType.DARE -> AccentPink
        CustomEntryType.STICKY_DARE -> AccentViolet
        CustomEntryType.TRIVIA -> AccentLime
    }

@get:StringRes
val CustomEntryType.labelRes: Int
    get() = when (this) {
        CustomEntryType.TRUTH -> R.string.custom_entry_type_truth
        CustomEntryType.DARE -> R.string.custom_entry_type_dare
        CustomEntryType.STICKY_DARE -> R.string.custom_entry_type_sticky
        CustomEntryType.TRIVIA -> R.string.custom_entry_type_trivia
    }
