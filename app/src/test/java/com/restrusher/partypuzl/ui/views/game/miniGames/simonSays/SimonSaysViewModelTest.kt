package com.restrusher.partypuzl.ui.views.game.miniGames.simonSays

import androidx.lifecycle.SavedStateHandle
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.testutil.MainDispatcherRule
import com.restrusher.partypuzl.testutil.TestPlayers
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SimonSaysViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: SimonSaysViewModel
    private val state get() = viewModel.uiState.value

    @Before
    fun setUp() {
        GamePlayersList.addPlayer(TestPlayers.alice)
        GamePlayersList.addPlayer(TestPlayers.bruno)
        viewModel = SimonSaysViewModel(SavedStateHandle())
    }

    @After
    fun tearDown() = GamePlayersList.resetPlayersList()

    @Test
    fun `the first round waits for input after showing one button`() = runTest(mainRule.dispatcher) {
        advanceUntilIdle()
        assertEquals(SimonSaysPhase.INPUT, state.phase)
        assertEquals(1, state.sequence.size)
    }

    @Test
    fun `taps before the sequence finishes showing are ignored`() = runTest(mainRule.dispatcher) {
        viewModel.onButtonTapped(0)
        assertEquals(SimonSaysPhase.COUNTDOWN, state.phase)
    }

    @Test
    fun `a wrong tap ends the game with the current player as loser`() = runTest(mainRule.dispatcher) {
        advanceUntilIdle()
        viewModel.onButtonTapped((state.sequence.first() + 1) % BUTTONS)
        assertEquals(SimonSaysPhase.GAME_OVER, state.phase)
        assertEquals(TestPlayers.alice, state.loser)
    }

    @Test
    fun `repeating the sequence passes the phone on`() = runTest(mainRule.dispatcher) {
        advanceUntilIdle()
        state.sequence.forEach(viewModel::onButtonTapped)
        assertEquals(SimonSaysPhase.PASS, state.phase)
        assertEquals(1, state.currentPlayerIndex)
    }

    @Test
    fun `after the last player the turn wraps back to the first with a longer sequence`() =
        runTest(mainRule.dispatcher) {
            advanceUntilIdle()
            state.sequence.forEach(viewModel::onButtonTapped)
            viewModel.onPassConfirmed()
            advanceUntilIdle()
            state.sequence.forEach(viewModel::onButtonTapped)
            assertEquals(0, state.currentPlayerIndex)
            assertEquals(2, state.sequence.size)
        }

    private companion object {
        const val BUTTONS = 4
    }
}
