package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.testutil.TestPlayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NameCyclePathTest {

    @Test
    fun `a two-player reel flicks between both names`() {
        repeat(50) {
            val path = NameCyclePath.of(listOf(TestPlayers.alice, TestPlayers.bruno), TestPlayers.alice)
            assertEquals(setOf("Alice", "Bruno"), path.steps.toSet())
            assertWellFormed(path, "Alice")
        }
    }

    @Test
    fun `a lone player lands straight away`() {
        val path = NameCyclePath.of(listOf(TestPlayers.alice), TestPlayers.alice)
        assertEquals(listOf("Alice"), path.steps)
    }

    @Test
    fun `a larger party never repeats a name back to back`() {
        val party = TestPlayers.roster + TestPlayers.unknown
        repeat(50) {
            val path = NameCyclePath.of(party, TestPlayers.cleo)
            assertEquals(12, path.steps.size)
            assertWellFormed(path, "Cleo")
        }
    }

    private fun assertWellFormed(path: NameCyclePath, picked: String) {
        assertEquals(picked, path.picked)
        assertNotEquals(picked, path.nameAt(path.lastStep - 1))
        assertTrue(path.steps.zipWithNext().none { (a, b) -> a == b })
    }
}
