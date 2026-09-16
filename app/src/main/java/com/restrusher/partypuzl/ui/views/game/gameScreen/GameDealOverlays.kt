package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.OUTCOME_STAGE_MS
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.OutcomeRevealContent
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.OutcomeSpinContent
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.activeOutcomeMode

/** The reward or punishment a finished turn raised, spinning before it lands. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun OutcomeOverlay(
    uiState: GameScreenState,
    onModeEventDismissed: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.activeOutcomeMode == null || uiState.activeEventCategory == null) return
    val interactionSource = remember { MutableInteractionSource() }
    val isPickingTarget = uiState.barMode.activeEvent is BarEvent.GiveDrinksPickTarget
    val isRevealed = uiState.outcomeStage == OutcomeStage.REVEALED

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isRevealed && !isPickingTarget
            ) { onModeEventDismissed() }
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = isRevealed,
                transitionSpec = {
                    fadeIn(tween(OUTCOME_STAGE_MS)) togetherWith fadeOut(tween(OUTCOME_STAGE_MS))
                },
                label = "outcome stage"
            ) { revealed ->
                if (revealed) {
                    OutcomeRevealContent(
                        uiState = uiState,
                        onGiveDrinksTargetSelected = onGiveDrinksTargetSelected,
                        animatedVisibilityScope = this@AnimatedContent,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    OutcomeSpinContent(
                        uiState = uiState,
                        animatedVisibilityScope = this@AnimatedContent,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/** The party-photo prompt, which flips itself face-up as it arrives. */
@Composable
internal fun CameraRequestCard(
    onCameraRequested: () -> Unit,
    onCameraRequestDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFlipped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isFlipped = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(dealCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(interactionSource = interactionSource, indication = null) {
                onCameraRequestDismissed()
            }
    ) {
        FlipCard(
            isFlipped = isFlipped,
            modifier = Modifier.fillMaxSize(),
            front = { Box(Modifier.fillMaxSize().background(Color.Transparent)) },
            back = {
                CameraRequestContent(
                    onCameraRequested = onCameraRequested,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    }
}
