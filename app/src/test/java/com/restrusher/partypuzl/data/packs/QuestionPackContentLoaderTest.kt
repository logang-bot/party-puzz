package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.DEFAULT_STICKY_DURATION_SECONDS
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.QuestionSource
import com.restrusher.partypuzl.data.models.TriviaPrompt
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionPackContentLoaderTest {

    private val questionRepository = mockk<QuestionRepository>()
    private val questionPackRepository = mockk<QuestionPackRepository>()
    private val customPackRepository = mockk<CustomPackRepository>()
    private val resolver = mockk<QuestionPromptResolver>()
    private val seeder = mockk<QuestionPackSeeder>(relaxed = true)
    private val loader = QuestionPackContentLoader(
        questionRepository, questionPackRepository, customPackRepository, resolver, seeder
    )

    @Before
    fun setUp() {
        coEvery { questionRepository.getPlayableQuestions() } returns emptyList()
        coEvery { customPackRepository.getPlayableEntries() } returns emptyList()
        coEvery { questionPackRepository.getPacksOnce() } returns emptyList()
        every { resolver.durationLabel(any()) } returns "label"
    }

    @Test
    fun `the catalog is seeded before any question is read`() = runTest {
        loader.loadEnabledContent()
        coVerifyOrder {
            seeder.seedIfNeeded()
            questionRepository.getPlayableQuestions()
        }
    }

    @Test
    fun `official rows are pooled into truths and dares by source`() = runTest {
        givenQuestions(question(QuestionSource.OFFICIAL_TRUTHS, 0), question(QuestionSource.NSFW_DARES, 1))
        every { resolver.textAt(QuestionSource.OFFICIAL_TRUTHS, 0) } returns "truth"
        every { resolver.textAt(QuestionSource.NSFW_DARES, 1) } returns "dare"
        val content = loader.loadEnabledContent()
        assertEquals(listOf("truth"), content.truths)
        assertEquals(listOf("dare"), content.dares)
    }

    @Test
    fun `a row that no longer resolves is dropped instead of crashing`() = runTest {
        givenQuestions(question(QuestionSource.OFFICIAL_TRUTHS, 99), question(QuestionSource.OFFICIAL_TRIVIA, 99))
        every { resolver.textAt(any(), any()) } returns null
        every { resolver.triviaAt(any(), any()) } returns null
        assertTrue(loader.loadEnabledContent().isEmpty)
    }

    @Test
    fun `custom truths and dares join the official pools`() = runTest {
        givenEntries(entry(CustomEntryType.TRUTH, "my truth"), entry(CustomEntryType.DARE, "my dare"))
        val content = loader.loadEnabledContent()
        assertEquals(listOf("my truth"), content.truths)
        assertEquals(listOf("my dare"), content.dares)
        assertTrue(PackCategory.TRUTH_OR_DARE in content.availableCategories)
    }

    @Test
    fun `custom trivia missing an option or answer is skipped`() = runTest {
        givenEntries(
            entry(CustomEntryType.TRIVIA, "complete").copy(optionA = "a", optionB = "b", correctOption = "B"),
            entry(CustomEntryType.TRIVIA, "no option b").copy(optionA = "a", correctOption = "A"),
            entry(CustomEntryType.TRIVIA, "no answer").copy(optionA = "a", optionB = "b")
        )
        assertEquals(listOf(TriviaPrompt("complete", "a", "b", 'B')), loader.loadEnabledContent().trivia)
    }

    @Test
    fun `a custom sticky dare falls back to the default duration and reuses its text`() = runTest {
        givenEntries(entry(CustomEntryType.STICKY_DARE, "hop on one leg"))
        val sticky = loader.loadEnabledContent().stickyDares.single()
        assertEquals(DEFAULT_STICKY_DURATION_SECONDS, sticky.durationSeconds)
        assertEquals("hop on one leg", sticky.presentContinuous)
        assertEquals("label", sticky.durationLabel)
    }

    @Test
    fun `mini-games are offered only when their pack is enabled`() = runTest {
        givenMiniGamePack(enabled = false)
        assertFalse(loader.loadEnabledContent().hasMiniGames)
        givenMiniGamePack(enabled = true)
        assertTrue(loader.loadEnabledContent().hasMiniGames)
    }

    private fun givenQuestions(vararg questions: QuestionEntity) {
        coEvery { questionRepository.getPlayableQuestions() } returns questions.toList()
    }

    private fun givenEntries(vararg entries: CustomEntryEntity) {
        coEvery { customPackRepository.getPlayableEntries() } returns entries.toList()
    }

    private fun givenMiniGamePack(enabled: Boolean) {
        val pack = QuestionPackEntity(
            QuestionPackCatalog.OFFICIAL_MINI_GAMES.id, PackTier.OFFICIAL, PackCategory.MINI_GAME, enabled
        )
        coEvery { questionPackRepository.getPacksOnce() } returns listOf(pack)
    }

    private fun question(source: QuestionSource, index: Int) =
        QuestionEntity(id = "${source.name}_$index", packId = "pack", source = source, sourceIndex = index)

    private fun entry(type: CustomEntryType, text: String) =
        CustomEntryEntity(text, "custom", type, text, null, null, null, null)
}
