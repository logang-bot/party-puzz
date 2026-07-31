package com.restrusher.partypuzl.ui.views.customPacks.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType

/**
 * The kinds of game deal step 01 of the entry form offers — one card each.
 *
 * Truth and dare share a card because they are one deal as the room experiences it: a player is
 * handed a prompt and answers it there and then. Which half it is becomes step 02.
 *
 * Behind the card they stay two separate [CustomEntryType]s, because the deck pools them apart —
 * the reveal screen offers a Truth card and a Dare card with no way to hide one, and a pack
 * holding only one half is worth warning about. Merging the picker does not merge the pools.
 *
 * Declared in the order the cards are drawn.
 */
enum class EntryDeal { TRUTH_OR_DARE, STICKY_DARE, TRIVIA }

/** Which card an entry sits under — the bridge back from a stored type to the picker. */
val CustomEntryType.deal: EntryDeal
    get() = when (this) {
        CustomEntryType.TRUTH, CustomEntryType.DARE -> EntryDeal.TRUTH_OR_DARE
        CustomEntryType.STICKY_DARE -> EntryDeal.STICKY_DARE
        CustomEntryType.TRIVIA -> EntryDeal.TRIVIA
    }

/** The type an entry starts as when the author picks this card. */
val EntryDeal.defaultType: CustomEntryType
    get() = when (this) {
        EntryDeal.TRUTH_OR_DARE -> CustomEntryType.TRUTH
        EntryDeal.STICKY_DARE -> CustomEntryType.STICKY_DARE
        EntryDeal.TRIVIA -> CustomEntryType.TRIVIA
    }

/**
 * The truth-or-dare card wears the flame, as in the design. The lightbulb is not lost — it marks
 * the Truth half of the step-02 toggle.
 */
@get:DrawableRes
val EntryDeal.iconRes: Int
    get() = when (this) {
        EntryDeal.TRUTH_OR_DARE -> CustomEntryType.DARE.iconRes
        EntryDeal.STICKY_DARE -> CustomEntryType.STICKY_DARE.iconRes
        EntryDeal.TRIVIA -> CustomEntryType.TRIVIA.iconRes
    }

/** One tone for the whole deal, so the page stops re-tinting when the author swaps halves. */
val EntryDeal.accent: Color
    get() = defaultType.accent

@get:StringRes
val EntryDeal.labelRes: Int
    get() = when (this) {
        EntryDeal.TRUTH_OR_DARE -> R.string.truth_or_dare
        EntryDeal.STICKY_DARE -> R.string.custom_entry_type_sticky
        EntryDeal.TRIVIA -> R.string.custom_entry_type_trivia
    }

@get:StringRes
val EntryDeal.hintRes: Int
    get() = when (this) {
        EntryDeal.TRUTH_OR_DARE -> R.string.custom_entry_type_truth_or_dare_hint
        EntryDeal.STICKY_DARE -> R.string.custom_entry_type_sticky_hint
        EntryDeal.TRIVIA -> R.string.custom_entry_type_trivia_hint
    }
