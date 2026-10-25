package com.restrusher.partypuzl.ui.views.game.miniGames.circleMaster

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.testutil.MainDispatcherRule
import com.restrusher.partypuzl.testutil.TestPlayers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class CircleMasterViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: CircleMasterViewModel
    private val state get() = viewModel.uiState.value

    @Before
    fun setUp() {
        GamePlayersList.addPlayer(TestPlayers.alice)
        GamePlayersList.addPlayer(TestPlayers.bruno)
        viewModel = CircleMasterViewModel(SavedStateHandle())
    }

    @After
    fun tearDown() = GamePlayersList.resetPlayersList()

    @Test
    fun `drawing is ignored until the countdown ends`() = runTest(mainRule.dispatcher) {
        viewModel.onDrawPoint(Offset(1f, 1f))
        assertTrue(state.drawnPath.isEmpty())
        advanceUntilIdle()
        assertEquals(CircleMasterPhase.DRAWING, state.phase)
    }

    @Test
    fun `a perfect circle scores near the top`() = runTest(mainRule.dispatcher) {
        assertTrue(scoreOf(ellipse(radiusX = 100f, radiusY = 100f)) >= 95)
    }

    @Test
    fun `a squashed loop scores below a round one`() = runTest(mainRule.dispatcher) {
        assertTrue(scoreOf(ellipse(radiusX = 150f, radiusY = 40f)) < 80)
    }

    @Test
    fun `a scribble too short to judge scores zero`() = runTest(mainRule.dispatcher) {
        assertEquals(0, scoreOf(ellipse(radiusX = 100f, radiusY = 100f).take(10)))
    }

    @Test
    fun `the next player starts with a clean canvas`() = runTest(mainRule.dispatcher) {
        scoreOf(ellipse(radiusX = 100f, radiusY = 100f))
        viewModel.onScoreAcknowledged()
        assertEquals(CircleMasterPhase.PASS, state.phase)
        viewModel.onPassConfirmed()
        assertEquals(TestPlayers.bruno, state.currentPlayer)
        assertTrue(state.drawnPath.isEmpty())
    }

    @Test
    fun `the best circle wins and the worst one loses`() = runTest(mainRule.dispatcher) {
        scoreOf(ellipse(radiusX = 100f, radiusY = 100f))
        viewModel.onScoreAcknowledged()
        viewModel.onPassConfirmed()
        draw(ellipse(radiusX = 100f, radiusY = 100f).take(5))
        viewModel.onScoreAcknowledged()
        assertEquals(CircleMasterPhase.WINNER, state.phase)
        assertEquals(TestPlayers.alice to TestPlayers.bruno, state.winner to state.loser)
    }

    private fun TestScope.scoreOf(path: List<Offset>): Int {
        advanceUntilIdle()
        draw(path)
        return state.currentScore ?: error("no score was revealed")
    }

    private fun draw(path: List<Offset>) {
        path.forEach(viewModel::onDrawPoint)
        viewModel.onDrawEnd()
    }

    private fun ellipse(radiusX: Float, radiusY: Float): List<Offset> = (0 until POINTS).map {
        val angle = 2 * Math.PI * it / POINTS
        Offset(CENTRE + radiusX * cos(angle).toFloat(), CENTRE + radiusY * sin(angle).toFloat())
    }

    private companion object {
        const val POINTS = 72
        const val CENTRE = 300f
    }
}
