package com.restrusher.partypuzl.data.packs

import android.content.Context
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.QuestionSource
import com.restrusher.partypuzl.data.models.StickyDarePrompt
import com.restrusher.partypuzl.data.models.TriviaPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a [QuestionSource] + index into the actual prompt, reading from resources so the text
 * stays in the user's language. This is the only place that maps a source to its `R.array`
 * ids — nothing persists them.
 *
 * Every accessor is bounds-checked. A stale index (array edited without bumping
 * `QuestionCatalog.MAPPING_VERSION`) yields null and is dropped from the deck rather than
 * crashing mid-party.
 */
@Singleton
class QuestionPromptResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Number of items in a source's primary array — the source of truth for drift checks. */
    fun sizeOf(source: QuestionSource): Int = strings(primaryArrayOf(source)).size

    /** Plain text for Truth and Dare sources. Null for any other source or an out-of-range index. */
    fun textAt(source: QuestionSource, index: Int): String? = when (source) {
        QuestionSource.OFFICIAL_TRUTHS -> strings(R.array.truth_texts).getOrNull(index)
        QuestionSource.OFFICIAL_DARES -> strings(R.array.dare_texts).getOrNull(index)
        QuestionSource.NSFW_TRUTHS -> strings(R.array.nsfw_truth_texts).getOrNull(index)
        QuestionSource.NSFW_DARES -> strings(R.array.nsfw_dare_texts).getOrNull(index)
        else -> null
    }

    fun stickyDareAt(source: QuestionSource, index: Int): StickyDarePrompt? {
        val arrays = when (source) {
            QuestionSource.OFFICIAL_STICKY_DARES -> StickyArrays(
                R.array.sticky_dares,
                R.array.sticky_dares_present_continuous,
                R.array.sticky_dares_duration_labels,
                R.array.sticky_dares_duration_seconds
            )

            QuestionSource.SPICY_STICKY_DARES -> StickyArrays(
                R.array.spicy_sticky_dares,
                R.array.spicy_sticky_dares_present_continuous,
                R.array.spicy_sticky_dares_duration_labels,
                R.array.spicy_sticky_dares_duration_seconds
            )

            else -> return null
        }
        val text = strings(arrays.dares).getOrNull(index) ?: return null
        val presentContinuous = strings(arrays.presentContinuous).getOrNull(index) ?: return null
        val label = strings(arrays.durationLabels).getOrNull(index) ?: return null
        val seconds = context.resources.getIntArray(arrays.durationSeconds).getOrNull(index)
            ?: return null
        return StickyDarePrompt(text, presentContinuous, label, seconds)
    }

    fun triviaAt(source: QuestionSource, index: Int): TriviaPrompt? {
        val arrays = when (source) {
            QuestionSource.OFFICIAL_TRIVIA -> TriviaArrays(
                R.array.gk_questions,
                R.array.gk_options_a,
                R.array.gk_options_b,
                R.array.gk_correct_options
            )

            QuestionSource.MOVIE_TRIVIA -> TriviaArrays(
                R.array.movie_gk_questions,
                R.array.movie_gk_options_a,
                R.array.movie_gk_options_b,
                R.array.movie_gk_correct_options
            )

            else -> return null
        }
        val question = strings(arrays.questions).getOrNull(index) ?: return null
        val optionA = strings(arrays.optionsA).getOrNull(index) ?: return null
        val optionB = strings(arrays.optionsB).getOrNull(index) ?: return null
        val correct = strings(arrays.correct).getOrNull(index)?.firstOrNull() ?: return null
        return TriviaPrompt(question, optionA, optionB, correct)
    }

    /**
     * Duration label for an authored sticky dare, e.g. "5 minutes".
     *
     * Official sticky dares carry a hand-written label in a parallel array. A custom one only has
     * the seconds the author picked, so the label is generated here — through a plurals resource,
     * so it still reads correctly in the user's language even though the dare text itself does not
     * translate.
     */
    fun durationLabel(seconds: Int): String {
        val minutes = (seconds / SECONDS_PER_MINUTE).coerceAtLeast(1)
        return context.resources.getQuantityString(
            R.plurals.sticky_dare_duration_minutes,
            minutes,
            minutes
        )
    }

    private fun primaryArrayOf(source: QuestionSource): Int = when (source) {
        QuestionSource.OFFICIAL_TRUTHS -> R.array.truth_texts
        QuestionSource.OFFICIAL_DARES -> R.array.dare_texts
        QuestionSource.OFFICIAL_STICKY_DARES -> R.array.sticky_dares
        QuestionSource.OFFICIAL_TRIVIA -> R.array.gk_questions
        QuestionSource.MOVIE_TRIVIA -> R.array.movie_gk_questions
        QuestionSource.SPICY_STICKY_DARES -> R.array.spicy_sticky_dares
        QuestionSource.NSFW_TRUTHS -> R.array.nsfw_truth_texts
        QuestionSource.NSFW_DARES -> R.array.nsfw_dare_texts
    }

    private fun strings(resId: Int): Array<String> = context.resources.getStringArray(resId)

    private data class StickyArrays(
        val dares: Int,
        val presentContinuous: Int,
        val durationLabels: Int,
        val durationSeconds: Int
    )

    private data class TriviaArrays(
        val questions: Int,
        val optionsA: Int,
        val optionsB: Int,
        val correct: Int
    )

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}
