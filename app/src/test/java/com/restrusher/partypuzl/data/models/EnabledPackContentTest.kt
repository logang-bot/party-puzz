package com.restrusher.partypuzl.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnabledPackContentTest {

    private val sticky = StickyDarePrompt("Talk like a pirate", "talking like a pirate", "5 min", 300)
    private val trivia = TriviaPrompt("2 + 2?", "4", "5", 'A')

    @Test
    fun `truth or dare needs both halves`() {
        assertTrue(EnabledPackContent(truths = listOf("t")).isEmpty)
        assertTrue(EnabledPackContent(dares = listOf("d")).isEmpty)
        assertEquals(
            setOf(PackCategory.TRUTH_OR_DARE),
            EnabledPackContent(truths = listOf("t"), dares = listOf("d")).availableCategories
        )
    }

    @Test
    fun `each other category is offered only when its pool has something`() {
        assertEquals(setOf(PackCategory.QUIZ), EnabledPackContent(trivia = listOf(trivia)).availableCategories)
        assertEquals(setOf(PackCategory.STICKY_DARE), EnabledPackContent(stickyDares = listOf(sticky)).availableCategories)
        assertEquals(setOf(PackCategory.MINI_GAME), EnabledPackContent(hasMiniGames = true).availableCategories)
    }

    @Test
    fun `an empty deck offers nothing`() {
        assertTrue(EnabledPackContent().isEmpty)
    }

    @Test
    fun `only the dare sources count as dares`() {
        val dares = QuestionSource.entries.filter { it.isDare }.toSet()
        assertEquals(setOf(QuestionSource.OFFICIAL_DARES, QuestionSource.NSFW_DARES), dares)
    }

    @Test
    fun `every dare source feeds truth or dare`() {
        assertTrue(QuestionSource.entries.filter { it.isDare }.all { it.category == PackCategory.TRUTH_OR_DARE })
    }
}
