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
        // Packs: add anything new, drop anything the catalog no longer defines. The second half
        // matters because splitting the flat decks retired the old one-pack-per-deal ids, and a
        // stale row would keep its questions alive through the foreign key.
        questionPackRepository.seedFromCatalog()
        questionPackRepository.deleteNotIn(QuestionPackCatalog.all.map { it.id })

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
