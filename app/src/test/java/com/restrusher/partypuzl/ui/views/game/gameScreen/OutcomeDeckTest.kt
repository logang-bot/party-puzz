package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.deckLandingOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reel renders the deck and stops at `deck.indexOf(activeEvent)`, so an outcome that is not in
 * its own deck is an outcome the reel cannot land on. Every path that raises one is checked here.
 */
class OutcomeDeckTest {

    private val players = listOf(
        Player(1, "Alice", Gender.Female, InterestedIn.Man),
        Player(2, "Bruno", Gender.Male, InterestedIn.Woman),
        Player(3, "Cleo", Gender.Female, InterestedIn.Both)
    )

    private val turn = GameScreenState(
        players = players,
        selectedPlayer = players.first(),
        miniGameOpponent = players[1]
    )

    @Test
    fun `bar punishment lands inside its own deck`() {
        repeat(REPEATS) {
            val bar = BarModeHandler().applyPunishment(turn, players.first()).barMode
            assertLandsInDeck(bar.activeEvent, bar.deck)
        }
    }

    @Test
    fun `bar reward lands inside its own deck`() {
        repeat(REPEATS) {
            val bar = BarModeHandler().applyReward(turn).barMode
            assertLandsInDeck(bar.activeEvent, bar.deck)
        }
    }

    @Test
    fun `bar mini-game outcomes land inside their own deck`() {
        results().forEach { result ->
            val bar = BarModeHandler().applyMiniGameResult(turn.copy(miniGameResult = result)).barMode
            assertLandsInDeck(bar.activeEvent, bar.deck)
        }
    }

    @Test
    fun `couples punishment lands inside its own deck`() {
        repeat(REPEATS) {
            val couples = CouplesModeHandler().applyPunishment(turn, players.first()).couplesMode
            assertLandsInDeck(couples.activeEvent, couples.deck)
        }
    }

    @Test
    fun `couples reward lands inside its own deck`() {
        repeat(REPEATS) {
            val couples = CouplesModeHandler().applyReward(turn).couplesMode
            assertLandsInDeck(couples.activeEvent, couples.deck)
        }
    }

    @Test
    fun `couples mini-game outcomes land inside their own deck`() {
        results().forEach { result ->
            val state = CouplesModeHandler().applyMiniGameResult(turn.copy(miniGameResult = result))
            val couples = state.couplesMode
            if (couples.activeEvent != null) assertLandsInDeck(couples.activeEvent, couples.deck)
        }
    }

    @Test
    fun `party puzl outcomes land inside whichever deck fired`() {
        repeat(REPEATS) {
            val state = PartyPuzlModeHandler().applyPunishment(turn, players.first())
            state.barMode.activeEvent?.let { assertLandsInDeck(it, state.barMode.deck) }
            state.couplesMode.activeEvent?.let { assertLandsInDeck(it, state.couplesMode.deck) }
        }
    }

    @Test
    fun `clearing an event empties the deck`() {
        val rolled = BarModeHandler().applyPunishment(turn, players.first())
        val cleared = BarModeHandler().clearEvent(rolled)
        assertEquals(emptyList<BarEvent>(), cleared.barMode.deck)
    }

    @Test
    fun `a mini-game outcome replaces the generic entry of its kind rather than joining it`() {
        val deck = listOf(BarEvent.FlavourPunishment(1), BarEvent.TakeDrinks(amount = 2))
        val landed = deckLandingOn(deck, BarEvent.TakeDrinks(amount = 5))
        assertEquals(deck.size, landed.size)
        assertEquals(listOf(BarEvent.FlavourPunishment(1), BarEvent.TakeDrinks(amount = 5)), landed)
    }

    @Test
    fun `an outcome with no counterpart in the deck is added`() {
        val deck = listOf<BarEvent>(BarEvent.FlavourReward(1))
        val landed = deckLandingOn(deck, BarEvent.NoAction)
        assertEquals(listOf(BarEvent.FlavourReward(1), BarEvent.NoAction), landed)
    }

    @Test
    fun `an outcome already in the deck leaves it untouched`() {
        val deck = listOf<BarEvent>(BarEvent.FlavourReward(1), BarEvent.NoAction)
        assertSame(deck, deckLandingOn(deck, BarEvent.NoAction))
    }

    @Test
    fun `every deck entry carries the category its roll asked for`() {
        val punishment = BarModeHandler().applyPunishment(turn, players.first()).barMode.deck
        assertTrue(punishment.all { it.category == EventCategory.PUNISHMENT })
        val reward = BarModeHandler().applyReward(turn).barMode.deck
        assertTrue(reward.all { it.category == EventCategory.REWARD })
        val couples = CouplesModeHandler().applyReward(turn).couplesMode.deck
        assertTrue(couples.all { it.category == EventCategory.REWARD })
    }

    private fun <T> assertLandsInDeck(fired: T?, deck: List<T>) {
        assertNotNull("no outcome was raised", fired)
        assertTrue("deck was empty", deck.isNotEmpty())
        assertTrue("$fired is not in $deck", fired in deck)
        assertEquals(1, deck.count { it == fired })
    }

    private fun results(): List<MiniGameResult> = listOf(
        ScoredMiniGameResult("Alice", 5, "Bruno", 1),
        ScoredMiniGameResult("Alice", 1, "Bruno", 5),
        ScoredMiniGameResult("Alice", 3, "Bruno", 3),
        LoserMiniGameResult("Alice")
    )

    private companion object {
        const val REPEATS = 60
    }
}
