package com.restrusher.partypuzl.data.packBank

import com.restrusher.partypuzl.data.models.TriviaPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackBankParserTest {

    private val parser = PackBankParser()

    @Test
    fun `a full document parses every kind of prompt`() {
        val content = parser.parse(document(FULL_PACK))!!
        val prompts = content.promptsFor("premium_mix", "en")!!
        assertEquals(7, content.contentVersion)
        assertEquals(listOf("t1"), prompts.truths)
        assertEquals(listOf("d1"), prompts.dares)
        assertEquals(listOf(RemoteStickyDare("s", "doing s", 180)), prompts.stickyDares)
        assertEquals(listOf(TriviaPrompt("q", "a", "b", 'B')), prompts.trivia)
    }

    @Test
    fun `an unknown schema version is rejected`() {
        assertNull(parser.parse("""{"schemaVersion": 2, "packs": {}}"""))
    }

    @Test
    fun `text that is not json is rejected`() {
        assertNull(parser.parse("<html>404</html>"))
    }

    @Test
    fun `malformed entries are skipped and the rest still load`() {
        val pack = """{"en": {
            "truths": ["ok", "", 3],
            "sticky": [{"text": "s"}, {"text": "s", "presentContinuous": "p", "durationSeconds": 60}],
            "trivia": [{"question": "q", "optionA": "a", "optionB": "b", "correct": "C"}]
        }}"""
        val prompts = parser.parse(document(pack))!!.promptsFor("premium_mix", "en")!!
        assertEquals(listOf("ok", "3"), prompts.truths)
        assertEquals(1, prompts.stickyDares.size)
        assertTrue(prompts.trivia.isEmpty())
    }

    @Test
    fun `a pack that is not an object is skipped`() {
        val json = """{"schemaVersion": 1, "packs": {"broken": [1], "premium_mix": $FULL_PACK}}"""
        assertEquals(setOf("premium_mix"), parser.parse(json)!!.packs.keys)
    }

    @Test
    fun `a missing language falls back to english`() {
        val content = parser.parse(document(FULL_PACK))!!
        assertEquals(listOf("t1"), content.promptsFor("premium_mix", "fr")!!.truths)
    }

    @Test
    fun `counts add up every prompt for the language`() {
        val content = parser.parse(document(FULL_PACK))!!
        assertEquals(mapOf("premium_mix" to 4), content.countsFor("es"))
    }

    private fun document(pack: String) =
        """{"schemaVersion": 1, "contentVersion": 7, "packs": {"premium_mix": $pack}}"""

    private companion object {
        const val FULL_PACK = """{"en": {
            "truths": ["t1"],
            "dares": ["d1"],
            "sticky": [{"text": "s", "presentContinuous": "doing s", "durationSeconds": 180}],
            "trivia": [{"question": "q", "optionA": "a", "optionB": "b", "correct": "B"}]
        }}"""
    }
}
