package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.views.game.common.PlayerPhoto

// ─── CardContent maps phase to what the main deal card shows ─────────────────

private enum class CardContent { TAP_TO_PLAY, CYCLING_NAMES, PLAYER_NAME, PLAYER_PHOTO }

private val GameDealPhase.cardContent
    get() = when (this) {
        GameDealPhase.IDLE -> CardContent.TAP_TO_PLAY
        GameDealPhase.ANIMATING -> CardContent.CYCLING_NAMES
        GameDealPhase.PLAYER_NAME_REVEAL -> CardContent.PLAYER_NAME
        GameDealPhase.PLAYER_PHOTO_REVEAL,
        GameDealPhase.CHALLENGE_SHOWN -> CardContent.PLAYER_PHOTO
    }

// ─── Game deal section ────────────────────────────────────────────────────────

@Composable
internal fun GameDealSection(
    uiState: GameScreenState,
    onGameDealTapped: () -> Unit,
    onChallengeDismissed: () -> Unit,
    onTruthOrDareChosen: (TruthOrDareChoice) -> Unit,
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
    val mainInteractionSource = remember { MutableInteractionSource() }
    val challengeInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // Main glass card — always visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(glassCardShape)
                .background(glassBrush)
                .border(1.dp, Color.White.copy(alpha = 0.3f), glassCardShape)
                .clickable(
                    interactionSource = mainInteractionSource,
                    indication = null,
                    enabled = uiState.dealPhase == GameDealPhase.IDLE
                ) { onGameDealTapped() }
        ) {
            GameDealMainContent(uiState = uiState)
        }

        // Challenge card — slides in on top when CHALLENGE_SHOWN
        AnimatedVisibility(
            visible = uiState.dealPhase == GameDealPhase.CHALLENGE_SHOWN,
            enter = scaleIn(tween(350), initialScale = 0.85f) + fadeIn(tween(300)),
            exit = scaleOut(tween(300), targetScale = 0.85f) + fadeOut(tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(glassCardShape)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), glassCardShape)
                    .clickable(
                        interactionSource = challengeInteractionSource,
                        indication = null,
                        enabled = run {
                            val barEvent = uiState.barMode.activeEvent
                            val isTapDismissibleBarEvent = barEvent != null && barEvent !is BarEvent.GiveDrinksPickTarget
                            val isTapDismissibleCouplesEvent = uiState.couplesMode.activeEvent != null
                            isTapDismissibleBarEvent ||
                            isTapDismissibleCouplesEvent ||
                            (uiState.isChallengeDismissible &&
                             !uiState.hasActiveModeEvent &&
                             !(uiState.isModeActive && uiState.dealType == GameDealType.MINI_GAME && uiState.miniGameResult != null))
                        }
                    ) {
                        val barEvent = uiState.barMode.activeEvent
                        when {
                            barEvent != null && barEvent !is BarEvent.GiveDrinksPickTarget -> onModeEventDismissed()
                            uiState.couplesMode.activeEvent != null -> onModeEventDismissed()
                            else -> onChallengeDismissed()
                        }
                    }
            ) {
                // Blurred photo behind the card content
                uiState.selectedPlayer?.let { player ->
                    PlayerPhoto(
                        player = player,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                renderEffect = BlurEffect(radiusX = 25f, radiusY = 25f)
                            }
                    )
                }

                // Dark overlay so text is legible over the blurred photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                )

                FlipCard(
                    isFlipped = uiState.hasActiveModeEvent,
                    modifier = Modifier.fillMaxSize(),
                    front = {
                        when (uiState.dealType) {
                            GameDealType.TRUTH_OR_DARE -> TruthOrDareChallengeContent(
                                uiState = uiState,
                                onTruthOrDareChosen = onTruthOrDareChosen,
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
                    },
                    back = {
                        ModeEventChallengeContent(
                            uiState = uiState,
                            onDismiss = onModeEventDismissed,
                            onGiveDrinksTargetSelected = onGiveDrinksTargetSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }
        }

        // Camera request card — slides in after a dare or mode event, then flips to reveal content
        val cameraInteractionSource = remember { MutableInteractionSource() }
        AnimatedVisibility(
            visible = uiState.showCameraRequest,
            enter = scaleIn(tween(350), initialScale = 0.85f) + fadeIn(tween(300)),
            exit = scaleOut(tween(300), targetScale = 0.85f) + fadeOut(tween(250))
        ) {
            var isCameraCardFlipped by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { isCameraCardFlipped = true }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(glassCardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = cameraInteractionSource,
                        indication = null
                    ) { onCameraRequestDismissed() }
            ) {
                FlipCard(
                    isFlipped = isCameraCardFlipped,
                    modifier = Modifier.fillMaxSize(),
                    front = {},
                    back = {
                        CameraRequestContent(
                            onCameraRequested = onCameraRequested,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }
        }
    }
}

// ─── Main card content (driven by phase) ─────────────────────────────────────

@Composable
private fun GameDealMainContent(
    uiState: GameScreenState,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = uiState.dealPhase.cardContent,
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
        label = "deal card content",
        modifier = modifier.fillMaxSize()
    ) { content ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (content) {
                CardContent.TAP_TO_PLAY -> Text(
                    text = stringResource(id = R.string.tap_to_play),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                CardContent.CYCLING_NAMES -> AnimatedContent(
                    targetState = uiState.animatingName,
                    transitionSpec = {
                        (slideInVertically(tween(220)) { -it } + fadeIn(tween(220))) togetherWith
                        (slideOutVertically(tween(220)) { it } + fadeOut(tween(220)))
                    },
                    label = "cycling name"
                ) { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                CardContent.PLAYER_NAME -> uiState.selectedPlayer?.let { player ->
                    Text(
                        text = player.nickName,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                CardContent.PLAYER_PHOTO -> uiState.selectedPlayer?.let { player ->
                    PlayerPhoto(
                        player = player,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
