package com.restrusher.partypuzl.ui.views.customPacks.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.PackTopic
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.AccentViolet

/**
 * How a custom pack's topic and its entries look.
 *
 * Everything colour-related lives here rather than in the data layer, which must not depend on
 * Compose — `CustomEntryType` stays a plain enum and gets its looks attached at the UI boundary,
 * the same way `QuestionPackDefinition` carries the built-in packs' spice.
 *
 * The pack's own icon and accent are *not* here: they come from its spice level, which built-in
 * packs share, so they live in `ui/common/SpiceLook.kt`.
 */

@get:StringRes
val PackTopic.labelRes: Int
    get() = when (this) {
        PackTopic.FRIENDS_INSIDE_JOKES -> R.string.pack_topic_friends_inside_jokes
        PackTopic.BIG_NIGHT_OUT -> R.string.pack_topic_big_night_out
        PackTopic.FOOD_AND_DRINK -> R.string.pack_topic_food_and_drink
        PackTopic.COUPLES -> R.string.pack_topic_couples
        PackTopic.FAMILY -> R.string.pack_topic_family
        PackTopic.WORK_CROWD -> R.string.pack_topic_work_crowd
        PackTopic.POP_CULTURE -> R.string.pack_topic_pop_culture
        PackTopic.TRAVEL -> R.string.pack_topic_travel
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
