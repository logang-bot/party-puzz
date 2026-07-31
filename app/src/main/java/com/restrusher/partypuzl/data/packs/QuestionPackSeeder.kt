package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionCatalog
import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Brings the `question_packs` and `questions` tables in line with the catalog.
 *
 * Runs before anything reads packs. Cheap and idempotent in the common case — the only real work
 * happens on a fresh install or after the mapping changes.
 */
@Singleton
class QuestionPackSeeder @Inject constructor(
    private val questionPackRepository: QuestionPackRepository,
    private val questionRepository: QuestionRepository,
    private val promptResolver: QuestionPromptResolver,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun seedIfNeeded() {
        syncPacks()
        syncQuestions()
    }

    /**
     * Add anything new, drop anything the catalog no longer defines, and — when the catalog
     * version has moved — rewrite the columns the catalog owns on the rows that already exist.
     *
     * The sweep matters because splitting the flat decks retired the old one-pack-per-deal ids,
     * and a stale row would keep its questions alive through the foreign key. The re-sync matters
     * because the insert above is `INSERT OR IGNORE`: without it, editing a definition would only
     * ever reach a device that had never launched the app. Custom packs are exempt from both —
     * they are never in the catalog by definition.
     */
    private suspend fun syncPacks() {
        questionPackRepository.seedFromCatalog()
        questionPackRepository.deleteRetiredCatalogPacks(QuestionPackCatalog.all.map { it.id })

        val storedVersion = userPreferencesRepository.getPackCatalogVersion()
        if (storedVersion == QuestionPackCatalog.CATALOG_VERSION) return

        questionPackRepository.resyncCatalog()
        userPreferencesRepository.setPackCatalogVersion(QuestionPackCatalog.CATALOG_VERSION)
    }

    /**
     * Rebuilds the `questions` table when the mapping changes. Only that table — authored text in
     * `custom_entries` is out of `replaceAll`'s reach, so a mapping bump never costs a user
     * anything they wrote.
     */
    private suspend fun syncQuestions() {
        val storedVersion = userPreferencesRepository.getQuestionMappingVersion()
        val hasQuestions = questionRepository.count() > 0
        if (storedVersion == QuestionCatalog.MAPPING_VERSION && hasQuestions) return

        questionRepository.replaceAll(buildQuestionRows())
        userPreferencesRepository.setQuestionMappingVersion(QuestionCatalog.MAPPING_VERSION)
    }

    /**
     * Turns the catalog's index lists into rows, dropping any index that no longer resolves.
     * That guard means an array edited without bumping `MAPPING_VERSION` yields a shorter deck
     * rather than a crash when the game tries to draw a missing prompt.
     */
    private fun buildQuestionRows(): List<QuestionEntity> =
        QuestionCatalog.allEntries().mapNotNull { (packId, ref) ->
            if (ref.index !in 0 until promptResolver.sizeOf(ref.source)) return@mapNotNull null
            QuestionEntity(
                id = "${ref.source.name}_${ref.index}",
                packId = packId,
                source = ref.source,
                sourceIndex = ref.index
            )
        }
}
