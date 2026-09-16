package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.restrusher.partypuzl.data.models.Player

private const val PHASE_ENTER_MS = 320
private const val PHASE_EXIT_MS = 220

/**
 * The turn, phase by phase. Every phase is a child of one [AnimatedContent], so each one enters
 * and leaves on the same cross-fade whichever direction the turn moves in; the surrounding
 * [SharedTransitionLayout] is what lets the picked player's photo and name travel out of the turn
 * intro and into the picker's header rather than cross-fading in place.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun GameDealSection(
    uiState: GameScreenState,
    onPlayerPickFinished: () -> Unit,
    onDealPromoted: (GameDealType) -> Unit,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    onSurpriseRequested: () -> Unit,
    onSurpriseSettled: () -> Unit,
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
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState.dealPhase,
                transitionSpec = {
                    (fadeIn(tween(PHASE_ENTER_MS)) +
                            scaleIn(tween(PHASE_ENTER_MS), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(PHASE_EXIT_MS)))
                },
                label = "deal phase",
                modifier = Modifier.fillMaxSize()
            ) { phase ->
                val revealScopes = PlayerRevealScopes(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
                when (phase) {
                    GameDealPhase.PLAYER_PICK -> PlayerPickContent(
                        uiState = uiState,
                        scopes = revealScopes,
                        onFinished = onPlayerPickFinished
                    )

                    GameDealPhase.DEAL_CHOICE -> DealChoiceContent(
                        uiState = uiState,
                        revealScopes = revealScopes,
                        onDealPromoted = onDealPromoted,
                        onDealChosen = onDealChosen,
                        onSurpriseRequested = onSurpriseRequested
                    )

                    GameDealPhase.SURPRISE_SPOTLIGHT -> SurpriseSpotlightContent(
                        uiState = uiState,
                        onSurpriseSettled = onSurpriseSettled
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
