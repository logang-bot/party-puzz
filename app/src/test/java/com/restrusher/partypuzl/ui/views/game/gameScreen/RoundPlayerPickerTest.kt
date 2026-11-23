package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.testutil.TestPlayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class RoundPlayerPickerTest {

    private val roster = TestPlayers.roster + TestPlayers.unknown

    @Test
    fun `an empty party has nobody to pick`() {
        assertNull(RoundPlayerPicker(Random(1)).pick(emptyList()))
    }

    @Test
    fun `every player plays exactly once per round`() {
        repeat(50) { seed ->
            val picks = pickMany(RoundPlayerPicker(Random(seed)), roster.size * 20)
            picks.chunked(roster.size).forEach { assertEquals(roster.toSet(), it.toSet()) }
        }
    }

    @Test
    fun `nobody plays twice in a row, even across rounds`() {
        repeat(50) { seed ->
            pickMany(RoundPlayerPicker(Random(seed)), 200).zipWithNext()
                .forEach { (a, b) -> assertNotEquals(a, b) }
        }
    }

    @Test
    fun `two players alternate`() {
        val pair = listOf(TestPlayers.alice, TestPlayers.bruno)
        val picker = RoundPlayerPicker(Random(9))
        val picks = List(20) { picker.pick(pair)!! }
        picks.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
    }

    @Test
    fun `a single player always plays`() {
        val picker = RoundPlayerPicker(Random(2))
        repeat(3) { assertEquals(TestPlayers.alice, picker.pick(listOf(TestPlayers.alice))) }
    }

    @Test
    fun `the round goes up once every player-count turns`() {
        val picker = RoundPlayerPicker(Random(3))
        val rounds = List(7) { picker.pick(TestPlayers.roster); picker.roundNumber }
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 3), rounds)
    }

    @Test
    fun `a removed player is skipped for the rest of the round`() {
        val picker = RoundPlayerPicker(Random(4))
        val first = picker.pick(roster)!!
        val remaining = roster - first
        val gone = remaining.first()
        val rest = List(remaining.size - 1) { picker.pick(roster - gone)!! }
        assertEquals((remaining - gone).toSet(), rest.toSet())
    }

    private fun pickMany(picker: RoundPlayerPicker, count: Int) =
        List(count) { picker.pick(roster)!! }
}
