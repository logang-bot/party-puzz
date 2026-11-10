package com.restrusher.partypuzl.data.packBank

import com.restrusher.partypuzl.data.models.TriviaPrompt
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * Reads `packs.json`. Tolerant on purpose: a malformed pack, language or entry is dropped and the
 * rest still loads. Only an unreadable document or an unknown `schemaVersion` rejects the whole
 * file, and then the caller keeps its cached copy.
 */
class PackBankParser @Inject constructor() {

    fun parse(json: String): PackBankContent? =
        runCatching { parseDocument(JSONObject(json)) }.getOrNull()

    private fun parseDocument(document: JSONObject): PackBankContent? {
        if (document.optInt(SCHEMA_KEY) != SUPPORTED_SCHEMA) return null
        val packs = document.getJSONObject(PACKS_KEY)
        return PackBankContent(
            contentVersion = document.optInt(CONTENT_VERSION_KEY),
            packs = packs.objectsByKey().mapValues { (_, pack) -> parsePack(pack) }
        )
    }

    private fun parsePack(pack: JSONObject): Map<String, RemotePackPrompts> =
        pack.objectsByKey().mapValues { (_, prompts) -> parsePrompts(prompts) }

    private fun parsePrompts(prompts: JSONObject) = RemotePackPrompts(
        truths = prompts.texts("truths"),
        dares = prompts.texts("dares"),
        stickyDares = prompts.objects("sticky").mapNotNull(::stickyDare),
        trivia = prompts.objects("trivia").mapNotNull(::trivia)
    )

    private fun stickyDare(entry: JSONObject): RemoteStickyDare? = runCatching {
        RemoteStickyDare(
            text = entry.requireText("text"),
            presentContinuous = entry.requireText("presentContinuous"),
            durationSeconds = entry.getInt("durationSeconds").also { require(it > 0) }
        )
    }.getOrNull()

    private fun trivia(entry: JSONObject): TriviaPrompt? = runCatching {
        TriviaPrompt(
            question = entry.requireText("question"),
            optionA = entry.requireText("optionA"),
            optionB = entry.requireText("optionB"),
            correctOption = entry.getString("correct").single().also { require(it in VALID_ANSWERS) }
        )
    }.getOrNull()

    private companion object {
        const val SUPPORTED_SCHEMA = 1
        const val SCHEMA_KEY = "schemaVersion"
        const val CONTENT_VERSION_KEY = "contentVersion"
        const val PACKS_KEY = "packs"
        val VALID_ANSWERS = setOf('A', 'B')
    }
}

private fun JSONObject.objectsByKey(): Map<String, JSONObject> =
    keys().asSequence().mapNotNull { key -> optJSONObject(key)?.let { key to it } }.toMap()

private fun JSONObject.texts(key: String): List<String> =
    optJSONArray(key).items { array, index -> array.optString(index).takeIf(String::isNotBlank) }

private fun JSONObject.objects(key: String): List<JSONObject> =
    optJSONArray(key).items { array, index -> array.optJSONObject(index) }

private fun <T> JSONArray?.items(read: (JSONArray, Int) -> T?): List<T> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { read(this, it) }

private fun JSONObject.requireText(key: String): String =
    getString(key).also { require(it.isNotBlank()) }
