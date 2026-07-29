package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.DEFAULT_STICKY_DURATION_SECONDS
import com.restrusher.partypuzl.data.models.EnabledPackContent
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.StickyDarePrompt
import com.restrusher.partypuzl.data.models.TriviaPrompt
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import javax.inject.Inject

/**
 * Turns "which packs are enabled" into "what can actually be drawn this game".
 *
 * Reads the question rows for enabled packs, resolves each one's text from resources, and pools
 * everything by category — so the game screen never has to know packs exist, it just asks for a
 * deck.
 */
class QuestionPackContentLoader @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val questionPackRepository: QuestionPackRepository,
    private val customPackRepository: CustomPackRepository,
    private val promptResolver: QuestionPromptResolver,
    private val seeder: QuestionPackSeeder
) {

    suspend fun loadEnabledContent(): EnabledPackContent {
        seeder.seedIfNeeded()

        val truths = mutableListOf<String>()
        val dares = mutableListOf<String>()
        val stickyDares = mutableListOf<StickyDarePrompt>()
        val trivia = mutableListOf<TriviaPrompt>()

        // A row that no longer resolves (array edited without bumping the mapping version) is
        // skipped rather than crashing the party.
        questionRepository.getPlayableQuestions().forEach { question ->
            when (question.source.category) {
                PackCategory.TRUTH_OR_DARE -> {
                    val text = promptResolver.textAt(question.source, question.sourceIndex)
                        ?: return@forEach
                    if (question.source.isDare) dares += text else truths += text
                }

                PackCategory.STICKY_DARE ->
                    promptResolver.stickyDareAt(question.source, question.sourceIndex)
                        ?.let { stickyDares += it }

                PackCategory.GENERAL_KNOWLEDGE ->
                    promptResolver.triviaAt(question.source, question.sourceIndex)
                        ?.let { trivia += it }

                // No source produces mini-games; that pack carries no question rows.
                PackCategory.MINI_GAME -> Unit
            }
        }

        customPackRepository.getPlayableEntries()
            .poolInto(truths, dares, stickyDares, trivia)

        return EnabledPackContent(
            truths = truths,
            dares = dares,
            stickyDares = stickyDares,
            trivia = trivia,
            hasMiniGames = isMiniGamePackEnabled()
        )
    }

    /**
     * Folds authored entries into the same pools as the official ones, so the game screen still
     * only ever sees one deck.
     *
     * Custom text bypasses [QuestionPromptResolver] entirely — the user wrote it, there is no
     * translation to look up. A malformed row (trivia missing an option) is skipped rather than
     * crashing, matching how an unresolvable official index is handled above.
     */
    private fun List<CustomEntryEntity>.poolInto(
        truths: MutableList<String>,
        dares: MutableList<String>,
        stickyDares: MutableList<StickyDarePrompt>,
        trivia: MutableList<TriviaPrompt>
    ) = forEach { entry ->
        when (entry.type) {
            CustomEntryType.TRUTH -> truths += entry.text
            CustomEntryType.DARE -> dares += entry.text
            CustomEntryType.STICKY_DARE -> stickyDares += entry.toStickyDare()
            CustomEntryType.TRIVIA -> entry.toTrivia()?.let { trivia += it }
        }
    }

    /**
     * The ticker reads "<player> is <presentContinuous> for <durationLabel>". Authoring only asks
     * for the dare itself, so the same text stands in for the present-continuous phrasing — it is
     * also the dedup key that stops one player collecting the same sticky dare twice, so it must
     * never be blank.
     */
    private fun CustomEntryEntity.toStickyDare(): StickyDarePrompt {
        val seconds = durationSeconds ?: DEFAULT_STICKY_DURATION_SECONDS
        return StickyDarePrompt(
            text = text,
            presentContinuous = text,
            durationLabel = promptResolver.durationLabel(seconds),
            durationSeconds = seconds
        )
    }

    private fun CustomEntryEntity.toTrivia(): TriviaPrompt? {
        val a = optionA ?: return null
        val b = optionB ?: return null
        val correct = correctOption?.firstOrNull() ?: return null
        return TriviaPrompt(question = text, optionA = a, optionB = b, correctOption = correct)
    }

    /** Mini-games are code rather than prompts, so their pack is checked directly. */
    private suspend fun isMiniGamePackEnabled(): Boolean =
        questionPackRepository.getPacksOnce().any {
            it.id == QuestionPackCatalog.OFFICIAL_MINI_GAMES.id && it.isEnabled
        }
}
