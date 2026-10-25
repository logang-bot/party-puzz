package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionCatalog
import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.models.QuestionSource
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionPackSeederTest {

    private val packRepository = mockk<QuestionPackRepository>(relaxed = true)
    private val questionRepository = mockk<QuestionRepository>(relaxed = true)
    private val resolver = mockk<QuestionPromptResolver>()
    private val preferences = mockk<UserPreferencesRepository>(relaxed = true)
    private val seeder = QuestionPackSeeder(packRepository, questionRepository, resolver, preferences)

    @Before
    fun setUp() {
        every { resolver.sizeOf(any()) } returns Int.MAX_VALUE
        givenStoredVersions(catalog = QuestionPackCatalog.CATALOG_VERSION, mapping = QuestionCatalog.MAPPING_VERSION)
        coEvery { questionRepository.count() } returns 1
    }

    @Test
    fun `new catalog packs are inserted and retired ones removed on every run`() = runTest {
        seeder.seedIfNeeded()
        coVerify { packRepository.seedFromCatalog() }
        coVerify { packRepository.deleteRetiredCatalogPacks(QuestionPackCatalog.all.map { it.id }) }
    }

    @Test
    fun `an up-to-date install does no rewriting`() = runTest {
        seeder.seedIfNeeded()
        coVerify(exactly = 0) { packRepository.resyncCatalog() }
        coVerify(exactly = 0) { questionRepository.replaceAll(any()) }
    }

    @Test
    fun `a catalog version bump resyncs packs and stores the new version`() = runTest {
        givenStoredVersions(catalog = 0, mapping = QuestionCatalog.MAPPING_VERSION)
        seeder.seedIfNeeded()
        coVerify { packRepository.resyncCatalog() }
        coVerify { preferences.setPackCatalogVersion(QuestionPackCatalog.CATALOG_VERSION) }
    }

    @Test
    fun `a mapping version bump rebuilds the questions and stores the new version`() = runTest {
        givenStoredVersions(catalog = QuestionPackCatalog.CATALOG_VERSION, mapping = 0)
        seeder.seedIfNeeded()
        coVerify { questionRepository.replaceAll(any()) }
        coVerify { preferences.setQuestionMappingVersion(QuestionCatalog.MAPPING_VERSION) }
    }

    @Test
    fun `an empty questions table is rebuilt even on the current mapping`() = runTest {
        coEvery { questionRepository.count() } returns 0
        seeder.seedIfNeeded()
        coVerify { questionRepository.replaceAll(any()) }
    }

    @Test
    fun `rebuilt rows cover the whole catalog with deterministic ids`() = runTest {
        val rows = rebuiltRows()
        assertEquals(QuestionCatalog.allEntries().size, rows.size)
        assertTrue(rows.all { it.id == "${it.source.name}_${it.sourceIndex}" })
    }

    @Test
    fun `indices past the end of their array are dropped`() = runTest {
        every { resolver.sizeOf(QuestionSource.MOVIE_TRIVIA) } returns 10
        val movieRows = rebuiltRows().filter { it.source == QuestionSource.MOVIE_TRIVIA }
        assertEquals((0 until 10).toList(), movieRows.map { it.sourceIndex })
    }

    private suspend fun rebuiltRows(): List<QuestionEntity> {
        coEvery { questionRepository.count() } returns 0
        val rows = slot<List<QuestionEntity>>()
        coEvery { questionRepository.replaceAll(capture(rows)) } returns Unit
        seeder.seedIfNeeded()
        return rows.captured
    }

    private fun givenStoredVersions(catalog: Int, mapping: Int) {
        coEvery { preferences.getPackCatalogVersion() } returns catalog
        coEvery { preferences.getQuestionMappingVersion() } returns mapping
    }
}
