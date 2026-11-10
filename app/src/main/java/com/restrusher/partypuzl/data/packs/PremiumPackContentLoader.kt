package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.models.EnabledPackContent
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.StickyDarePrompt
import com.restrusher.partypuzl.data.packBank.PackBankRepository
import com.restrusher.partypuzl.data.packBank.RemotePackPrompts
import com.restrusher.partypuzl.data.packBank.RemoteStickyDare
import com.restrusher.partypuzl.data.packBank.currentPackLanguage
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import javax.inject.Inject

/**
 * The premium half of the deck. Premium prompts come from the downloaded pack bank rather than
 * `questions` rows, so this reads the enabled premium packs and pulls their prompts from it. A pack
 * that hasn't downloaded yet simply contributes nothing.
 */
class PremiumPackContentLoader @Inject constructor(
    private val questionPackRepository: QuestionPackRepository,
    private val packBankRepository: PackBankRepository,
    private val promptResolver: QuestionPromptResolver
) {

    suspend fun load(): EnabledPackContent {
        packBankRepository.loadCache()
        val content = packBankRepository.content.value
        val language = currentPackLanguage()
        return enabledPremiumPackIds()
            .mapNotNull { content.promptsFor(it, language) }
            .fold(EnabledPackContent()) { deck, prompts -> deck + prompts.toDeck() }
    }

    private suspend fun enabledPremiumPackIds(): List<String> =
        questionPackRepository.getPacksOnce()
            .filter { it.tier == PackTier.PREMIUM && it.isEnabled }
            .map { it.id }

    private fun RemotePackPrompts.toDeck() = EnabledPackContent(
        truths = truths,
        dares = dares,
        stickyDares = stickyDares.map { it.toPrompt() },
        trivia = trivia
    )

    private fun RemoteStickyDare.toPrompt() = StickyDarePrompt(
        text = text,
        presentContinuous = presentContinuous,
        durationLabel = promptResolver.durationLabel(durationSeconds),
        durationSeconds = durationSeconds
    )
}
