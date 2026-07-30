package com.restrusher.partypuzl.data.packs

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionCatalog
import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.QuestionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guards the seam between [QuestionCatalog]'s hardcoded indices and the `string-array`s they point
 * at.
 *
 * That seam is only checked by eye today, and it fails quietly in both directions: an index past
 * the end of its array is dropped by `QuestionPackSeeder` (a silently shorter deck), and an array
 * longer than its declared range leaves prompts that are never dealt at all. Neither shows up as
 * a crash, in a build, or in a smoke test.
 *
 * Instrumented rather than a plain unit test because every check needs real resources — and it
 * needs the *localised* ones, since `QuestionPromptResolver` reads whichever locale is active.
 */
@RunWith(AndroidJUnit4::class)
class QuestionPackIntegrityTest {

    private lateinit var resolver: QuestionPromptResolver

    @Before
    fun setUp() {
        resolver = QuestionPromptResolver(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    /** Every index the catalog declares must produce a prompt, for every pack. */
    @Test
    fun everyCatalogedQuestionResolves() {
        QuestionCatalog.allEntries().forEach { (packId, ref) ->
            val prompt: Any? = when (ref.source.category) {
                PackCategory.TRUTH_OR_DARE -> resolver.textAt(ref.source, ref.index)
                PackCategory.STICKY_DARE -> resolver.stickyDareAt(ref.source, ref.index)
                PackCategory.GENERAL_KNOWLEDGE -> resolver.triviaAt(ref.source, ref.index)
                PackCategory.MINI_GAME -> Unit
            }
            assertNotNull(
                "$packId declares ${ref.source}[${ref.index}], which does not resolve",
                prompt
            )
        }
    }

    /**
     * Premium packs own their arrays outright — they take every index — so the declared count and
     * the array length must agree exactly. This is the half that catches orphaned content.
     */
    @Test
    fun premiumPacksClaimTheirWholeArray() {
        QuestionPackCatalog.tier(PackTier.PREMIUM).forEach { definition ->
            QuestionCatalog.questionsFor(definition.id)
                .groupBy { it.source }
                .forEach { (source, refs) ->
                    assertEquals(
                        "${definition.id} declares ${refs.size} of ${source.name}, " +
                                "but the array holds ${resolver.sizeOf(source)}",
                        resolver.sizeOf(source),
                        refs.size
                    )
                }
        }
    }

    /** A trivia answer key that is neither A nor B leaves the question unanswerable. */
    @Test
    fun everyTriviaKeyIsAOrB() {
        QuestionCatalog.allEntries()
            .filter { (_, ref) -> ref.source.category == PackCategory.GENERAL_KNOWLEDGE }
            .forEach { (packId, ref) ->
                val trivia = resolver.triviaAt(ref.source, ref.index)
                assertNotNull("$packId: ${ref.source}[${ref.index}] did not resolve", trivia)
                assertTrue(
                    "$packId: ${ref.source}[${ref.index}] has key '${trivia!!.correctOption}'",
                    trivia.correctOption == 'A' || trivia.correctOption == 'B'
                )
            }
    }

    /**
     * `EnabledPackContent.availableCategories` only offers Truth or Dare when the deck holds both
     * halves, so a Truth-or-Dare pack that ships only one is unplayable on its own.
     */
    @Test
    fun truthOrDarePacksShipBothHalves() {
        QuestionPackCatalog.all
            .filter { it.category == PackCategory.TRUTH_OR_DARE }
            .forEach { definition ->
                val sources = QuestionCatalog.questionsFor(definition.id).map { it.source }
                assertTrue(
                    "${definition.id} has no truths",
                    sources.any { !it.isDare && it.category == PackCategory.TRUTH_OR_DARE }
                )
                assertTrue(
                    "${definition.id} has no dares",
                    sources.any { it.isDare }
                )
            }
    }

    /**
     * Sticky dares are assembled from four parallel arrays, and the resolver returns null the
     * moment one is short — which would read as a missing dare rather than a mismatched array.
     */
    @Test
    fun stickyDareArraysAreParallel() {
        listOf(QuestionSource.OFFICIAL_STICKY_DARES, QuestionSource.SPICY_STICKY_DARES)
            .forEach { source ->
                repeat(resolver.sizeOf(source)) { index ->
                    val prompt = resolver.stickyDareAt(source, index)
                    assertNotNull("$source[$index]: parallel arrays disagree", prompt)
                    assertTrue(
                        "$source[$index] has a blank present-continuous, which is the ticker " +
                                "phrasing and the per-player dedup key",
                        prompt!!.presentContinuous.isNotBlank()
                    )
                }
            }
    }
}
