package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.pow

private const val GATHER_MS = 350
private const val TRAVEL_FAST_STEP_MS = 45
private const val TRAVEL_SLOW_STEP_MS = 260
private const val TRAVEL_STEPS_MIN = 12
private const val TRAVEL_RAMP_EXPONENT = 3f
private const val FLICKER_STEPS = 4
private const val LANDED_HOLD_MS = 200

internal enum class SurpriseSpotlightStage { GATHER, TRAVEL, FLICKER, LANDED }

/**
 * "Surprise me", staged over [SurpriseSpotlightStage].
 *
 * Purely cosmetic: the deal was chosen the instant the button was tapped. [onSurpriseSettled]
 * hands the turn back to the picker with that deal promoted, so the phase cross-fade grows it into
 * the hero slot and the player confirms it with the same tap a hand-picked category needs.
 *
 * Keyed on `surpriseRequestId` because the phase `AnimatedContent` keeps this composition alive
 * while it animates out: a player who re-taps inside that window would otherwise land back in a
 * reused composition still holding [SurpriseSpotlightStage.LANDED], and if the fresh random pick
 * matched the last one the timeline would never restart.
 */
@Composable
internal fun SurpriseSpotlightContent(
    uiState: GameScreenState,
    onSurpriseSettled: () -> Unit,
    modifier: Modifier = Modifier
) {
    key(uiState.surpriseRequestId) {
        SpotlightTimeline(uiState = uiState, onSurpriseSettled = onSurpriseSettled, modifier = modifier)
    }
}

@Composable
private fun SpotlightTimeline(
    uiState: GameScreenState,
    onSurpriseSettled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val landed = uiState.surpriseDealType
    var stage by remember { mutableStateOf(SurpriseSpotlightStage.GATHER) }
    var highlighted by remember { mutableStateOf<GameDealType?>(null) }

    LaunchedEffect(landed) {
        if (landed == null) return@LaunchedEffect
        delay(GATHER_MS.toLong())
        stage = SurpriseSpotlightStage.TRAVEL
        travelHighlight(SpotlightTravelPath(uiState.availableDealTypes, landed)) { highlighted = it }
        stage = SurpriseSpotlightStage.FLICKER
        delay((FLICKER_STEPS * FLICKER_STEP_MS).toLong())
        stage = SurpriseSpotlightStage.LANDED
        delay(LANDED_HOLD_MS.toLong())
        onSurpriseSettled()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        CurrentPlayerHeader(player = uiState.selectedPlayer)
        Spacer(Modifier.height(20.dp))
        SpotlightGrid(
            dealTypes = uiState.availableDealTypes,
            highlightedDealType = highlighted,
            isFlickering = stage == SurpriseSpotlightStage.FLICKER,
            gatherMillis = GATHER_MS
        )
    }
}

private data class SpotlightTravelPath(
    val dealTypes: List<GameDealType>,
    val landed: GameDealType
) {
    val stepCount: Int get() = wholeLoops * dealTypes.size + dealTypes.indexOf(landed) + 1

    private val wholeLoops: Int
        get() = ceil(TRAVEL_STEPS_MIN.toDouble() / dealTypes.size).toInt()

    fun dealTypeAt(step: Int): GameDealType = dealTypes[step % dealTypes.size]
}

private suspend fun travelHighlight(
    path: SpotlightTravelPath,
    onStep: (GameDealType) -> Unit
) {
    repeat(path.stepCount) { step ->
        onStep(path.dealTypeAt(step))
        delay(travelStepMillis(step, path.stepCount).toLong())
    }
}

/**
 * How long the ring rests on the card reached at [step] of [stepCount].
 *
 * The cube of the progress fraction keeps nearly every step close to [TRAVEL_FAST_STEP_MS] and
 * stretches only the last two or three towards [TRAVEL_SLOW_STEP_MS], so the ring reads as slowing
 * *onto* a card rather than easing uniformly across all of them.
 */
private fun travelStepMillis(step: Int, stepCount: Int): Int {
    val progress = (step / (stepCount - 1).coerceAtLeast(1).toFloat()).pow(TRAVEL_RAMP_EXPONENT)
    return (TRAVEL_FAST_STEP_MS + (TRAVEL_SLOW_STEP_MS - TRAVEL_FAST_STEP_MS) * progress).toInt()
}

private val spotlightPreviewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bruno", Gender.Male, InterestedIn.Woman)
)

private val spotlightPreviewState = GameScreenState(
    players = spotlightPreviewPlayers,
    selectedPlayer = spotlightPreviewPlayers.first(),
    dealPhase = GameDealPhase.SURPRISE_SPOTLIGHT,
    surpriseDealType = GameDealType.STICKY_DARE
)

@Preview(name = "SurpriseSpotlight – Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SurpriseSpotlightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize()) {
            SurpriseSpotlightContent(uiState = spotlightPreviewState, onSurpriseSettled = {})
        }
    }
}

@Preview(name = "SurpriseSpotlight – Dark", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SurpriseSpotlightDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize()) {
            SurpriseSpotlightContent(uiState = spotlightPreviewState, onSurpriseSettled = {})
        }
    }
}
