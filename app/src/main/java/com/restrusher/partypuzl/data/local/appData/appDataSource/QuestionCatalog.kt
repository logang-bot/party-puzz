package com.restrusher.partypuzl.data.local.appData.appDataSource

import com.restrusher.partypuzl.data.models.QuestionRef
import com.restrusher.partypuzl.data.models.QuestionSource

/**
 * Which questions belong to which pack.
 *
 * Indices are **0-based positions in the source `string-array`**. That is the fragile part of
 * keeping text in resources and membership in the database, so two rules protect it:
 *
 * 1. **Source arrays are append-only.** Never reorder or delete an item — add to the end.
 *    Reordering silently reassigns questions to the wrong packs.
 * 2. **Editing an array means bumping [MAPPING_VERSION].** The seeder rebuilds every question
 *    row when the stored version differs, which repairs any drift. Per-question enable flags are
 *    reset by that rebuild; pack-level toggles are untouched.
 *
 * [QuestionPackSeeder] additionally drops any index that no longer resolves, so even an
 * un-bumped edit degrades to a shorter deck instead of a crash.
 */
object QuestionCatalog {

    /** Bump whenever any source array is edited. Forces a rebuild of the question rows. */
    const val MAPPING_VERSION = 1

    // ── Truth or Dare ────────────────────────────────────────────────────────
    // truth_texts (40) and dare_texts (52) split across four themed packs.

    private val ICEBREAKER_TRUTHS = listOf(2, 7, 14, 16, 17, 21, 34)
    private val ICEBREAKER_DARES =
        listOf(1, 2, 6, 13, 17, 20, 22, 23, 32, 33, 34, 35, 41, 42, 44, 45)

    private val CONFESSION_TRUTHS =
        listOf(0, 4, 5, 10, 11, 15, 18, 22, 23, 24, 25, 29, 30, 31, 36, 37, 38, 39)
    private val CONFESSION_DARES = listOf(4, 7, 14, 15, 19, 21, 29, 36)

    private val PARTY_ANIMAL_TRUTHS = listOf(3, 9, 12, 20, 35)
    private val PARTY_ANIMAL_DARES =
        listOf(0, 8, 9, 10, 16, 18, 24, 25, 26, 27, 28, 30, 31, 37, 38, 39, 40, 43, 50, 51)

    // "This Room" absorbed the Love & Exes questions — both are about the people around you.
    private val THIS_ROOM_TRUTHS = listOf(1, 6, 8, 13, 19, 26, 27, 28, 32, 33)
    private val THIS_ROOM_DARES = listOf(3, 5, 11, 12, 46, 47, 48, 49)

    // ── Sticky dares ─────────────────────────────────────────────────────────
    // sticky_dares (22) split by what the dare actually constrains.

    private val VOICES_ACCENTS = listOf(0, 3, 4, 7, 8, 12, 20)
    private val VERBAL_TICS = listOf(1, 5, 6, 9, 14, 17, 18)
    private val BODY_PERSONA = listOf(2, 10, 11, 13, 15, 16, 19, 21)

    // ── Trivia ───────────────────────────────────────────────────────────────
    // gk_questions (30) split by subject.

    private val GEOGRAPHY = listOf(1, 2, 8, 10, 15, 18, 21, 24, 28)
    private val SPACE_SCIENCE = listOf(0, 4, 5, 6, 7, 12, 13, 16, 17, 19, 22, 29)
    private val MIXED_BAG = listOf(3, 9, 11, 14, 20, 23, 25, 26, 27)

    /**
     * packId → the questions it contains. Packs absent from this map contribute no text
     * questions; today that is only the mini-games pack, which is code rather than prompts.
     */
    private val byPack: Map<String, List<QuestionRef>> = mapOf(
        QuestionPackCatalog.OFFICIAL_ICEBREAKERS.id to
                refs(QuestionSource.OFFICIAL_TRUTHS, ICEBREAKER_TRUTHS) +
                refs(QuestionSource.OFFICIAL_DARES, ICEBREAKER_DARES),

        QuestionPackCatalog.OFFICIAL_CONFESSIONS.id to
                refs(QuestionSource.OFFICIAL_TRUTHS, CONFESSION_TRUTHS) +
                refs(QuestionSource.OFFICIAL_DARES, CONFESSION_DARES),

        QuestionPackCatalog.OFFICIAL_PARTY_ANIMALS.id to
                refs(QuestionSource.OFFICIAL_TRUTHS, PARTY_ANIMAL_TRUTHS) +
                refs(QuestionSource.OFFICIAL_DARES, PARTY_ANIMAL_DARES),

        QuestionPackCatalog.OFFICIAL_THIS_ROOM.id to
                refs(QuestionSource.OFFICIAL_TRUTHS, THIS_ROOM_TRUTHS) +
                refs(QuestionSource.OFFICIAL_DARES, THIS_ROOM_DARES),

        QuestionPackCatalog.OFFICIAL_VOICES_ACCENTS.id to
                refs(QuestionSource.OFFICIAL_STICKY_DARES, VOICES_ACCENTS),

        QuestionPackCatalog.OFFICIAL_VERBAL_TICS.id to
                refs(QuestionSource.OFFICIAL_STICKY_DARES, VERBAL_TICS),

        QuestionPackCatalog.OFFICIAL_BODY_PERSONA.id to
                refs(QuestionSource.OFFICIAL_STICKY_DARES, BODY_PERSONA),

        QuestionPackCatalog.OFFICIAL_GEOGRAPHY.id to
                refs(QuestionSource.OFFICIAL_TRIVIA, GEOGRAPHY),

        QuestionPackCatalog.OFFICIAL_SPACE_SCIENCE.id to
                refs(QuestionSource.OFFICIAL_TRIVIA, SPACE_SCIENCE),

        QuestionPackCatalog.OFFICIAL_MIXED_BAG.id to
                refs(QuestionSource.OFFICIAL_TRIVIA, MIXED_BAG),

        // Premium packs own their arrays outright, so they take every index.
        QuestionPackCatalog.PREMIUM_MOVIE_NIGHT.id to
                refs(QuestionSource.MOVIE_TRIVIA, 0 until 12),

        QuestionPackCatalog.PREMIUM_SPICY.id to
                refs(QuestionSource.SPICY_STICKY_DARES, 0 until 8),

        QuestionPackCatalog.PREMIUM_NSFW.id to
                refs(QuestionSource.NSFW_TRUTHS, 0 until 10) +
                refs(QuestionSource.NSFW_DARES, 0 until 10)
    )

    fun questionsFor(packId: String): List<QuestionRef> = byPack[packId].orEmpty()

    /** Every (packId, question) pair, in catalog order. Used to build the seed. */
    fun allEntries(): List<Pair<String, QuestionRef>> =
        byPack.entries.flatMap { (packId, refs) -> refs.map { packId to it } }

    private fun refs(source: QuestionSource, indices: Iterable<Int>): List<QuestionRef> =
        indices.map { QuestionRef(source, it) }
}
