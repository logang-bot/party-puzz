package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.testutil.TestPlayers
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The love declaration has to be aimed at someone the chooser is actually into. */
class CouplesPunishmentTargetTest {

    @Test
    fun `the target matches who the chooser is interested in`() {
        val targets = targetsOf(TestPlayers.roster, chooser = TestPlayers.alice)
        assertEquals(setOf("Bruno"), targets)
    }

    @Test
    fun `a player of unknown gender suits every taste`() {
        val players = listOf(TestPlayers.bruno, TestPlayers.alice, TestPlayers.unknown)
        assertEquals(setOf("Alice", "Sam"), targetsOf(players, chooser = TestPlayers.bruno))
    }

    @Test
    fun `with nobody to their taste anyone else is picked`() {
        val players = listOf(TestPlayers.alice, TestPlayers.cleo)
        assertEquals(setOf("Cleo"), targetsOf(players, chooser = TestPlayers.alice))
    }

    @Test
    fun `the chooser is never their own target`() {
        val targets = targetsOf(TestPlayers.roster, chooser = TestPlayers.cleo)
        assertTrue("Cleo" !in targets)
        assertEquals(setOf("Alice", "Bruno"), targets)
    }

    @Test
    fun `playing solo leaves the target blank`() {
        assertEquals(setOf(""), targetsOf(listOf(TestPlayers.alice), chooser = TestPlayers.alice))
    }

    private fun targetsOf(players: List<Player>, chooser: Player): Set<String> =
        (1..REPEATS).map { declarationTarget(couplesPunishmentDeck(players, chooser)) }.toSet()

    private fun declarationTarget(deck: List<CouplesEvent>): String =
        deck.filterIsInstance<CouplesEvent.MakeALoveDeclaration>().single().targetPlayerName

    private companion object {
        const val REPEATS = 60
    }
}
