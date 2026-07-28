package com.restrusher.partypuzl.data.models

/** One sticky dare with its parallel metadata already zipped together. */
data class StickyDarePrompt(
    val text: String,
    val presentContinuous: String,
    val durationLabel: String,
    val durationSeconds: Int
)

/** One general-knowledge question with its two options. */
data class TriviaPrompt(
    val question: String,
    val optionA: String,
    val optionB: String,
    val correctOption: Char
)

/**
 * The playable deck for one game: every prompt from every enabled question in every enabled
 * pack, pooled by category. Built once when the game screen opens — packs can't be toggled
 * mid-game.
 */
data class EnabledPackContent(
    val truths: List<String> = emptyList(),
    val dares: List<String> = emptyList(),
    val stickyDares: List<StickyDarePrompt> = emptyList(),
    val trivia: List<TriviaPrompt> = emptyList(),
    val hasMiniGames: Boolean = false
) {
    /**
     * Categories that actually have something to play. A category with nothing behind it is
     * dropped from the deal picker and the surprise reel.
     *
     * Truth or Dare needs **both** halves: the reveal screen offers a Truth card and a Dare card
     * with no way to hide one, so a deck with truths but no dares would hand back an empty
     * prompt when someone picked Dare.
     */
    val availableCategories: Set<PackCategory>
        get() = buildSet {
            if (truths.isNotEmpty() && dares.isNotEmpty()) add(PackCategory.TRUTH_OR_DARE)
            if (trivia.isNotEmpty()) add(PackCategory.GENERAL_KNOWLEDGE)
            if (stickyDares.isNotEmpty()) add(PackCategory.STICKY_DARE)
            if (hasMiniGames) add(PackCategory.MINI_GAME)
        }

    val isEmpty: Boolean get() = availableCategories.isEmpty()
}
