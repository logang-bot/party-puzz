package com.restrusher.partypuzl.data.models

/**
 * How bold a custom pack reads. Purely cosmetic: it picks the pack's icon and accent colour and
 * shows as a chip, nothing more. The built-in packs express intensity by membership instead
 * (`premium_spicy`, `NSFW_*`), so nothing filters on this.
 */
enum class SpiceLevel { MILD, MEDIUM, SPICY }

/**
 * What one authored entry plays as.
 *
 * Mirrors [QuestionSource.category] so `QuestionPackContentLoader` can fan out over custom entries
 * with the same `when` it already uses for official ones. [TRUTH] and [DARE] are separate values
 * for the same reason `OFFICIAL_TRUTHS` and `OFFICIAL_DARES` are: the reveal screen pools them
 * apart, and a pack with only one half is worth warning about.
 *
 * There is deliberately no mini-game type — mini-games are code, not prompts.
 */
enum class CustomEntryType(val category: PackCategory) {
    TRUTH(PackCategory.TRUTH_OR_DARE),
    DARE(PackCategory.TRUTH_OR_DARE),
    STICKY_DARE(PackCategory.STICKY_DARE),
    TRIVIA(PackCategory.GENERAL_KNOWLEDGE)
}

/** Durations a sticky dare can be given, in seconds: 1, 5, 10 and 30 minutes. */
val StickyDurationPresets = listOf(60, 300, 600, 1800)

/** Default picked when the author opens the sticky-dare form. */
const val DEFAULT_STICKY_DURATION_SECONDS = 300

/** Longest an authored prompt may be — matches the character counters on the authoring screens. */
const val ENTRY_TEXT_MAX = 180
const val TRIVIA_TEXT_MAX = 160
const val PACK_DESCRIPTION_MAX = 140

/** Everything the user typed on the create/edit pack screen. */
data class CustomPackDraft(
    val id: String? = null,
    val name: String,
    val description: String,
    val category: PackCategory,
    val spice: SpiceLevel
)

/**
 * Everything the user typed on the entry screen. Only the fields the picked [type] needs are
 * filled — the rest stay null and are never read.
 */
data class CustomEntryDraft(
    val id: String? = null,
    val packId: String,
    val type: CustomEntryType,
    val text: String,
    val durationSeconds: Int? = null,
    val optionA: String? = null,
    val optionB: String? = null,
    val correctOption: Char? = null
)
