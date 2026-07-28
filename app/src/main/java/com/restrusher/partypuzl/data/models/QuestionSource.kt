package com.restrusher.partypuzl.data.models

/**
 * Which resource deck a question's text comes from.
 *
 * Questions are categorised in the database but their text stays in `strings.xml`, so a row has
 * to name its source deck. It names it with **this enum, never with an `R.array` id** — resource
 * ids are regenerated on every build and can shift, so a persisted one silently starts pointing
 * at the wrong array. Room stores enum constants by name, which is stable.
 *
 * [category] is the deal the source feeds. Truth and Dare are separate sources because a
 * Truth-or-Dare pack draws from both and has to keep them apart.
 */
enum class QuestionSource(val category: PackCategory) {
    OFFICIAL_TRUTHS(PackCategory.TRUTH_OR_DARE),
    OFFICIAL_DARES(PackCategory.TRUTH_OR_DARE),
    OFFICIAL_STICKY_DARES(PackCategory.STICKY_DARE),
    OFFICIAL_TRIVIA(PackCategory.GENERAL_KNOWLEDGE),
    MOVIE_TRIVIA(PackCategory.GENERAL_KNOWLEDGE),
    SPICY_STICKY_DARES(PackCategory.STICKY_DARE),
    NSFW_TRUTHS(PackCategory.TRUTH_OR_DARE),
    NSFW_DARES(PackCategory.TRUTH_OR_DARE);

    /** True for the Dare half of a Truth-or-Dare source. */
    val isDare: Boolean get() = this == OFFICIAL_DARES || this == NSFW_DARES
}

/** One question, addressed by its source deck and position within it. */
data class QuestionRef(
    val source: QuestionSource,
    val index: Int
)
