package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.OutcomeRevealContent
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.OutcomeSpinContent
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.activeOutcomeMode
import com.restrusher.partypuzl.ui.views.game.gameScreen.outcome.reelIndex

@Composable
internal fun GameDealSection(
    uiState: GameScreenState,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    onSurpriseRequested: () -> Unit,
    onChallengeDismissed: () -> Unit,
    onTruthOrDareSkipped: () -> Unit,
    onStickyDareSkipped: () -> Unit,
    onMiniGameDealFinished: () -> Unit,
    onGeneralKnowledgeAnswered: (Char) -> Unit,
    onMiniGameOpponentSelected: (Player) -> Unit,
    onGlobalMiniGameStarted: () -> Unit,
    onModeEventDismissed: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
    onCameraRequested: () -> Unit,
    onCameraRequestDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = uiState.dealPhase,
            transitionSpec = {
                (fadeIn(tween(320)) + scaleIn(tween(320), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(220)))
            },
            label = "deal phase",
            modifier = Modifier.fillMaxSize()
        ) { phase ->
            when (phase) {
                GameDealPhase.DEAL_CHOICE -> DealChoiceContent(
                    uiState = uiState,
                    onDealChosen = onDealChosen,
                    onSurpriseRequested = onSurpriseRequested
                )

                GameDealPhase.SURPRISE_SHUFFLE -> SurpriseShuffleContent(
                    dealTypes = uiState.availableDealTypes,
                    targetDealType = uiState.surpriseDealType
                )

                GameDealPhase.CHALLENGE_SHOWN -> ChallengeContent(
                    uiState = uiState,
                    onChallengeDismissed = onChallengeDismissed,
                    onTruthOrDareSkipped = onTruthOrDareSkipped,
                    onStickyDareSkipped = onStickyDareSkipped,
                    onMiniGameDealFinished = onMiniGameDealFinished,
                    onGeneralKnowledgeAnswered = onGeneralKnowledgeAnswered,
                    onMiniGameOpponentSelected = onMiniGameOpponentSelected,
                    onGlobalMiniGameStarted = onGlobalMiniGameStarted
                )
            }
        }

        // Reward / punishment sits above the turn — a dare cancelled from the sticky-dares sheet
        // can raise one at any phase, not just after a challenge.
        AnimatedVisibility(
            visible = uiState.hasActiveModeEvent,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200))
        ) {
            OutcomeOverlay(
                uiState = uiState,
                onModeEventDismissed = onModeEventDismissed,
                onGiveDrinksTargetSelected = onGiveDrinksTargetSelected
            )
        }

        AnimatedVisibility(
            visible = uiState.showCameraRequest,
            enter = scaleIn(tween(350), initialScale = 0.85f) + fadeIn(tween(300)),
            exit = scaleOut(tween(300), targetScale = 0.85f) + fadeOut(tween(250))
        ) {
            CameraRequestCard(
                onCameraRequested = onCameraRequested,
                onCameraRequestDismissed = onCameraRequestDismissed
            )
        }
    }
}

@Composable
private fun ChallengeContent(
    uiState: GameScreenState,
    onChallengeDismissed: () -> Unit,
    onTruthOrDareSkipped: () -> Unit,
    onStickyDareSkipped: () -> Unit,
    onMiniGameDealFinished: () -> Unit,
    onGeneralKnowledgeAnswered: (Char) -> Unit,
    onMiniGameOpponentSelected: (Player) -> Unit,
    onGlobalMiniGameStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = uiState.isChallengeDismissible &&
                        !uiState.hasActiveModeEvent &&
                        !(uiState.isModeActive &&
                                uiState.dealType == GameDealType.MINI_GAME &&
                                uiState.miniGameResult != null)
            ) { onChallengeDismissed() }
    ) {
        when (uiState.dealType) {
            GameDealType.TRUTH_OR_DARE -> TruthOrDareChallengeContent(
                uiState = uiState,
                onSkipped = onTruthOrDareSkipped,
                modifier = Modifier.fillMaxSize()
            )

            GameDealType.STICKY_DARE -> StickyDareChallengeContent(
                uiState = uiState,
                onSkipped = onStickyDareSkipped,
                modifier = Modifier.fillMaxSize()
            )

            GameDealType.GENERAL_KNOWLEDGE -> GeneralKnowledgeChallengeContent(
                uiState = uiState,
                onAnswerSelected = onGeneralKnowledgeAnswered,
                modifier = Modifier.fillMaxSize()
            )

            GameDealType.MINI_GAME -> MiniGameChallengeContent(
                uiState = uiState,
                onOpponentSelected = onMiniGameOpponentSelected,
                onGlobalMiniGameStarted = onGlobalMiniGameStarted,
                onFinished = onMiniGameDealFinished,
                modifier = Modifier.fillMaxSize()
            )

            null -> Unit
        }
    }
}

@Composable
private fun OutcomeOverlay(
    uiState: GameScreenState,
    onModeEventDismissed: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = uiState.activeOutcomeMode ?: return
    val category = uiState.activeEventCategory ?: return
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
        if (isRevealed) {
            OutcomeRevealContent(
                uiState = uiState,
                onGiveDrinksTargetSelected = onGiveDrinksTargetSelected,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            OutcomeSpinContent(
                mode = mode,
                category = category,
                targetIndex = uiState.couplesMode.activeEvent?.reelIndex
                    ?: uiState.barMode.activeEvent?.reelIndex
                    ?: 0,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CameraRequestCard(
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
