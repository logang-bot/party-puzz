package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.testutil.TestPlayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WeightedPlayerPickerTest {

    private val pair = listOf(TestPlayers.alice, TestPlayers.bruno)

    @Test
    fun `an empty party has nobody to pick`() {
        assertNull(WeightedPlayerPicker(Random(1)).pick(emptyList()))
    }

    @Test
    fun `two players both get turns and repeats do happen`() {
        val picks = pickMany(WeightedPlayerPicker(Random(7)), 400)
        assertEquals(pair.toSet(), picks.toSet())
        assertTrue(picks.zipWithNext().any { (a, b) -> a == b })
    }

    @Test
    fun `the player who just played is less likely to go again`() {
        val picks = pickMany(WeightedPlayerPicker(Random(42)), 2000)
        val repeatRate = picks.zipWithNext().count { (a, b) -> a == b } / (picks.size - 1.0)
        assertTrue("repeat rate was $repeatRate", repeatRate in 0.2..0.45)
    }

    @Test
    fun `the round goes up once every player-count turns`() {
        val picker = WeightedPlayerPicker(Random(3))
        val rounds = List(7) { picker.pick(TestPlayers.roster); picker.roundNumber(3) }
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 3), rounds)
    }

    private fun pickMany(picker: WeightedPlayerPicker, count: Int) =
        List(count) { picker.pick(pair)!! }
}
