package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.models.EnabledPackContent
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.StickyDarePrompt
import com.restrusher.partypuzl.data.models.TriviaPrompt
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

        return EnabledPackContent(
            truths = truths,
            dares = dares,
            stickyDares = stickyDares,
            trivia = trivia,
            hasMiniGames = isMiniGamePackEnabled()
        )
    }

    /** Mini-games are code rather than prompts, so their pack is checked directly. */
    private suspend fun isMiniGamePackEnabled(): Boolean =
        questionPackRepository.getPacksOnce().any {
            it.id == QuestionPackCatalog.OFFICIAL_MINI_GAMES.id && it.isEnabled
        }
}
