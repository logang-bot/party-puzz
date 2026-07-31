package com.restrusher.partypuzl.data.local.appData.appDataSource

import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appModels.QuestionPackDefinition
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.SpiceLevel

/**
 * The built-in question packs.
 *
 * A pack declares only its identity and how spicy it reads. Its questions are listed in
 * [QuestionCatalog] and stored as rows in the `questions` table; the prompt text itself stays in
 * `strings.xml` so it remains translatable. Room stores nothing but the user's on/off and unlock
 * state.
 *
 * Packs are thematic rather than one-per-deal: the original flat decks were split so a group can
 * pick the *kind* of night they want. Because a pack still feeds exactly one deal, switching off
 * every pack of a category is what removes that deal from the game.
 *
 * The icon and accent are *not* declared — they come from [QuestionPackDefinition.spice], which
 * is the same three-level vocabulary the create-pack screen offers. That is deliberate: a curated
 * pack should not be able to wear a look a user could never give one of their own.
 */
object QuestionPackCatalog {

    /**
     * Bumped whenever a definition below changes something that is *stored* — its tier or its
     * category. `QuestionPackSeeder` only inserts rows it has never seen, so without this an
     * edit here would never reach a device that has already launched the app. Names, spice and
     * question membership are read from code and need no bump.
     */
    const val CATALOG_VERSION = 1

    // ── Official · Truth or Dare ─────────────────────────────────────────────

    val OFFICIAL_ICEBREAKERS = QuestionPackDefinition(
        id = "official_icebreakers",
        tier = PackTier.OFFICIAL,
        category = PackCategory.TRUTH_OR_DARE,
        nameRes = R.string.pack_icebreakers,
        spice = SpiceLevel.MILD
    )

    val OFFICIAL_CONFESSIONS = QuestionPackDefinition(
        id = "official_confessions",
        tier = PackTier.OFFICIAL,
        category = PackCategory.TRUTH_OR_DARE,
        nameRes = R.string.pack_confessions,
        spice = SpiceLevel.MEDIUM
    )

    val OFFICIAL_PARTY_ANIMALS = QuestionPackDefinition(
        id = "official_party_animals",
        tier = PackTier.OFFICIAL,
        category = PackCategory.TRUTH_OR_DARE,
        nameRes = R.string.pack_party_animals,
        spice = SpiceLevel.SPICY
    )

    val OFFICIAL_THIS_ROOM = QuestionPackDefinition(
        id = "official_this_room",
        tier = PackTier.OFFICIAL,
        category = PackCategory.TRUTH_OR_DARE,
        nameRes = R.string.pack_this_room,
        spice = SpiceLevel.MEDIUM
    )

    // ── Official · Sticky dares ──────────────────────────────────────────────

    val OFFICIAL_VOICES_ACCENTS = QuestionPackDefinition(
        id = "official_voices_accents",
        tier = PackTier.OFFICIAL,
        category = PackCategory.STICKY_DARE,
        nameRes = R.string.pack_voices_accents,
        spice = SpiceLevel.MILD
    )

    val OFFICIAL_VERBAL_TICS = QuestionPackDefinition(
        id = "official_verbal_tics",
        tier = PackTier.OFFICIAL,
        category = PackCategory.STICKY_DARE,
        nameRes = R.string.pack_verbal_tics,
        spice = SpiceLevel.MILD
    )

    val OFFICIAL_BODY_PERSONA = QuestionPackDefinition(
        id = "official_body_persona",
        tier = PackTier.OFFICIAL,
        category = PackCategory.STICKY_DARE,
        nameRes = R.string.pack_body_persona,
        spice = SpiceLevel.MEDIUM
    )

    // ── Official · Trivia ────────────────────────────────────────────────────

    val OFFICIAL_GEOGRAPHY = QuestionPackDefinition(
        id = "official_geography",
        tier = PackTier.OFFICIAL,
        category = PackCategory.GENERAL_KNOWLEDGE,
        nameRes = R.string.pack_world_geography,
        spice = SpiceLevel.MILD
    )

    val OFFICIAL_SPACE_SCIENCE = QuestionPackDefinition(
        id = "official_space_science",
        tier = PackTier.OFFICIAL,
        category = PackCategory.GENERAL_KNOWLEDGE,
        nameRes = R.string.pack_space_science,
        spice = SpiceLevel.MILD
    )

    val OFFICIAL_MIXED_BAG = QuestionPackDefinition(
        id = "official_mixed_bag",
        tier = PackTier.OFFICIAL,
        category = PackCategory.GENERAL_KNOWLEDGE,
        nameRes = R.string.pack_mixed_bag,
        spice = SpiceLevel.MEDIUM
    )

    // ── Official · Mini-games ────────────────────────────────────────────────
    // The only pack with no question rows — mini-games are code, not prompts.

    val OFFICIAL_MINI_GAMES = QuestionPackDefinition(
        id = "official_mini_games",
        tier = PackTier.OFFICIAL,
        category = PackCategory.MINI_GAME,
        nameRes = R.string.pack_mini_games,
        spice = SpiceLevel.MEDIUM
    )

    // ── Premium ──────────────────────────────────────────────────────────────

    val PREMIUM_MOVIE_NIGHT = QuestionPackDefinition(
        id = "premium_movie_night",
        tier = PackTier.PREMIUM,
        category = PackCategory.GENERAL_KNOWLEDGE,
        nameRes = R.string.pack_movie_night,
        spice = SpiceLevel.MEDIUM
    )

    val PREMIUM_SPICY = QuestionPackDefinition(
        id = "premium_spicy",
        tier = PackTier.PREMIUM,
        category = PackCategory.STICKY_DARE,
        nameRes = R.string.pack_spicy,
        spice = SpiceLevel.SPICY
    )

    val PREMIUM_NSFW = QuestionPackDefinition(
        id = "premium_nsfw",
        tier = PackTier.PREMIUM,
        category = PackCategory.TRUTH_OR_DARE,
        nameRes = R.string.pack_nsfw_confessions,
        spice = SpiceLevel.SPICY
    )

    /** Display order on the setup screen: official grouped by deal, then premium. */
    val all: List<QuestionPackDefinition> = listOf(
        OFFICIAL_ICEBREAKERS,
        OFFICIAL_CONFESSIONS,
        OFFICIAL_PARTY_ANIMALS,
        OFFICIAL_THIS_ROOM,
        OFFICIAL_VOICES_ACCENTS,
        OFFICIAL_VERBAL_TICS,
        OFFICIAL_BODY_PERSONA,
        OFFICIAL_GEOGRAPHY,
        OFFICIAL_SPACE_SCIENCE,
        OFFICIAL_MIXED_BAG,
        OFFICIAL_MINI_GAMES,
        PREMIUM_MOVIE_NIGHT,
        PREMIUM_SPICY,
        PREMIUM_NSFW
    )

    private val byId: Map<String, QuestionPackDefinition> = all.associateBy { it.id }

    fun definitionFor(id: String): QuestionPackDefinition? = byId[id]

    fun tier(tier: PackTier): List<QuestionPackDefinition> = all.filter { it.tier == tier }

    /**
     * Rows for a fresh install: official packs on, premium packs off and locked. Used with
     * `INSERT OR IGNORE` so a user's existing choices survive catalog additions, and as the
     * source of the catalog-owned columns when [CATALOG_VERSION] moves.
     */
    fun defaultEntities(): List<QuestionPackEntity> = all.map { definition ->
        QuestionPackEntity(
            id = definition.id,
            tier = definition.tier,
            category = definition.category,
            isEnabled = definition.tier == PackTier.OFFICIAL,
            isUnlocked = definition.tier != PackTier.PREMIUM
        )
    }
}
