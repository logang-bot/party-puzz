package com.restrusher.partypuzl.data.packBank

import com.restrusher.partypuzl.data.models.TriviaPrompt

/**
 * The downloaded premium packs: pack id → language → prompts. Empty until the first download
 * succeeds, and kept as-is whenever a later one fails.
 */
data class PackBankContent(
    val contentVersion: Int,
    val packs: Map<String, Map<String, RemotePackPrompts>>
) {

    /** The user's language when the pack has it, English otherwise. */
    fun promptsFor(packId: String, language: String): RemotePackPrompts? =
        packs[packId]?.let { it[language] ?: it[FALLBACK_LANGUAGE] }

    fun countsFor(language: String): Map<String, Int> =
        packs.keys.associateWith { promptsFor(it, language)?.size ?: 0 }

    companion object {
        const val FALLBACK_LANGUAGE = "en"
        val EMPTY = PackBankContent(contentVersion = 0, packs = emptyMap())
    }
}

/** One premium pack in one language. A pack only fills the lists its game mode uses. */
data class RemotePackPrompts(
    val truths: List<String> = emptyList(),
    val dares: List<String> = emptyList(),
    val stickyDares: List<RemoteStickyDare> = emptyList(),
    val trivia: List<TriviaPrompt> = emptyList()
) {
    val size: Int get() = truths.size + dares.size + stickyDares.size + trivia.size
}

/** The duration label is not stored; the app builds it from the seconds in the user's language. */
data class RemoteStickyDare(
    val text: String,
    val presentContinuous: String,
    val durationSeconds: Int
)
