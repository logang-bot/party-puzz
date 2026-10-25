package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.testutil.TestPlayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameScreenStateTest {

    private val party = GameScreenState(players = TestPlayers.roster)

    @Test
    fun `the higher score wins a scored mini-game`() {
        assertEquals("Alice", ScoredMiniGameResult("Alice", 4, "Bruno", 2).winner)
        assertEquals("Bruno", ScoredMiniGameResult("Alice", 1, "Bruno", 2).winner)
    }

    @Test
    fun `a tied mini-game has no winner`() {
        assertNull(ScoredMiniGameResult("Alice", 3, "Bruno", 3).winner)
    }

    @Test
    fun `deal types follow the enabled pack categories`() {
        val state = party.copy(enabledCategories = setOf(PackCategory.QUIZ, PackCategory.STICKY_DARE))
        assertEquals(listOf(GameDealType.STICKY_DARE, GameDealType.QUIZ), state.availableDealTypes)
    }

    @Test
    fun `mini-games need at least two players`() {
        val solo = party.copy(players = listOf(TestPlayers.alice))
        assertFalse(GameDealType.MINI_GAME in solo.availableDealTypes)
        assertTrue(GameDealType.MINI_GAME in party.availableDealTypes)
    }

    @Test
    fun `an unavailable hero falls back to the first available deal`() {
        val state = party.copy(
            enabledCategories = setOf(PackCategory.QUIZ),
            heroDealType = GameDealType.STICKY_DARE
        )
        assertEquals(GameDealType.QUIZ, state.resolvedHeroDealType)
    }

    @Test
    fun `with nothing available the hero defaults to truth or dare`() {
        val state = party.copy(enabledCategories = emptySet(), heroDealType = GameDealType.QUIZ)
        assertEquals(GameDealType.TRUTH_OR_DARE, state.resolvedHeroDealType)
    }

    @Test
    fun `a promoted deal takes the hero slot only when it is available`() {
        val promoted = party.copy(promotedDealType = GameDealType.QUIZ)
        assertEquals(GameDealType.QUIZ, promoted.pickerHeroDealType)
        val unavailable = promoted.copy(enabledCategories = setOf(PackCategory.TRUTH_OR_DARE))
        assertEquals(GameDealType.TRUTH_OR_DARE, unavailable.pickerHeroDealType)
    }

    @Test
    fun `compact deals are every available deal except the hero`() {
        val state = party.copy(heroDealType = GameDealType.STICKY_DARE)
        assertFalse(GameDealType.STICKY_DARE in state.compactDealTypes)
        assertEquals(state.availableDealTypes.size - 1, state.compactDealTypes.size)
    }

    @Test
    fun `a challenge is dismissible only once the deal has been answered`() {
        assertFalse(party.isChallengeDismissible)
        assertTrue(party.copy(dealType = GameDealType.STICKY_DARE).isChallengeDismissible)
        val quiz = party.copy(dealType = GameDealType.QUIZ)
        assertFalse(quiz.isChallengeDismissible)
        assertTrue(quiz.copy(selectedAnswerOption = 'A').isChallengeDismissible)
    }

    @Test
    fun `truth or dare and mini-games need a choice or result before dismissing`() {
        val tod = party.copy(dealType = GameDealType.TRUTH_OR_DARE)
        assertFalse(tod.isChallengeDismissible)
        assertTrue(tod.copy(truthOrDareChoice = TruthOrDareChoice.DARE).isChallengeDismissible)
        val mini = party.copy(dealType = GameDealType.MINI_GAME)
        assertFalse(mini.isChallengeDismissible)
        assertTrue(mini.copy(miniGameResult = LoserMiniGameResult("Alice")).isChallengeDismissible)
    }

    @Test
    fun `the bar no-action tie is not a reward or punishment`() {
        val tie = party.copy(barMode = BarModeState(activeEvent = BarEvent.NoAction))
        assertTrue(tie.hasActiveModeEvent)
        assertFalse(tie.hasRewardOrPunishment)
    }

    @Test
    fun `the active event category reflects whichever mode fired`() {
        val bar = party.copy(barMode = BarModeState(activeEvent = BarEvent.TakeDrinks(2)))
        assertEquals(EventCategory.PUNISHMENT, bar.activeEventCategory)
        val couples = party.copy(couplesMode = CouplesModeState(activeEvent = CouplesEvent.GiveAKiss))
        assertEquals(EventCategory.REWARD, couples.activeEventCategory)
        assertNull(party.activeEventCategory)
    }

    @Test
    fun `the selected player stays hidden until the turn intro reveals them`() {
        val picking = party.copy(selectedPlayer = TestPlayers.alice, dealPhase = GameDealPhase.PLAYER_PICK)
        assertNull(picking.revealedPlayer)
        assertEquals(TestPlayers.alice, picking.copy(dealPhase = GameDealPhase.DEAL_CHOICE).revealedPlayer)
    }
}
