package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.testutil.TestPlayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** What each handler fires. `OutcomeDeckTest` covers whether the reel can land on it. */
class GameModeHandlerTest {

    private val turn = GameScreenState(
        players = TestPlayers.roster,
        selectedPlayer = TestPlayers.alice,
        miniGameOpponent = TestPlayers.bruno
    )

    @Test
    fun `winning a bar mini-game gives drinks to the opponent`() {
        val event = barEventFor(ScoredMiniGameResult("Alice", 5, "Bruno", 1))
        assertTrue(event is BarEvent.GiveDrinks)
        assertEquals("Bruno", (event as BarEvent.GiveDrinks).targetPlayerName)
    }

    @Test
    fun `losing a bar mini-game means taking drinks`() {
        assertTrue(barEventFor(ScoredMiniGameResult("Alice", 1, "Bruno", 5)) is BarEvent.TakeDrinks)
        assertTrue(barEventFor(LoserMiniGameResult("Alice")) is BarEvent.TakeDrinks)
    }

    @Test
    fun `a tied bar mini-game takes no action`() {
        assertEquals(BarEvent.NoAction, barEventFor(ScoredMiniGameResult("Alice", 2, "Bruno", 2)))
    }

    @Test
    fun `a mini-game without a result leaves the state untouched`() {
        assertSame(turn, BarModeHandler().applyMiniGameResult(turn))
        assertSame(turn, CouplesModeHandler().applyMiniGameResult(turn))
    }

    @Test
    fun `a tied couples mini-game leaves the state untouched`() {
        val tie = turn.copy(miniGameResult = ScoredMiniGameResult("Alice", 2, "Bruno", 2))
        assertSame(tie, CouplesModeHandler().applyMiniGameResult(tie))
    }

    @Test
    fun `the couples mini-game loser gets a punishment`() {
        val lost = turn.copy(miniGameResult = LoserMiniGameResult("Bruno"))
        val event = CouplesModeHandler().applyMiniGameResult(lost).couplesMode.activeEvent
        assertEquals(EventCategory.PUNISHMENT, event?.category)
    }

    @Test
    fun `the give-drinks target picker never offers the current player`() {
        val event = BarModeState.giveDrinksPickTargetEvent(TestPlayers.roster, TestPlayers.alice)
        assertEquals(listOf("Bruno", "Cleo"), event.candidates)
    }

    @Test
    fun `drink amounts always stay between one and five`() {
        repeat(REPEATS) {
            assertTrue(BarModeState.takeDrinksEvent().amount in 1..5)
            assertTrue(BarModeState.giveDrinksEvent("Bruno").amount in 1..5)
        }
    }

    @Test
    fun `the no-op handler changes nothing`() {
        val handler = NoOpModeHandler()
        assertSame(turn, handler.applyPunishment(turn, TestPlayers.alice))
        assertSame(turn, handler.applyReward(turn))
        assertSame(turn, handler.clearEvent(turn))
    }

    @Test
    fun `party puzl clears both modes at once`() {
        val rolled = turn.copy(
            barMode = BarModeState(activeEvent = BarEvent.NoAction, deck = listOf(BarEvent.NoAction)),
            couplesMode = CouplesModeState(activeEvent = CouplesEvent.GiveAKiss, deck = listOf(CouplesEvent.GiveAKiss))
        )
        val cleared = PartyPuzlModeHandler().clearEvent(rolled)
        assertNull(cleared.barMode.activeEvent)
        assertNull(cleared.couplesMode.activeEvent)
        assertTrue(cleared.barMode.deck.isEmpty() && cleared.couplesMode.deck.isEmpty())
    }

    private fun barEventFor(result: MiniGameResult): BarEvent? =
        BarModeHandler().applyMiniGameResult(turn.copy(miniGameResult = result)).barMode.activeEvent

    private companion object {
        const val REPEATS = 60
    }
}
