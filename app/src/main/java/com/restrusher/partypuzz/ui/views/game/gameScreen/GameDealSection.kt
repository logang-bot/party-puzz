package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    onModeEventDismissed: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
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
                            isTapDismissibleBarEvent ||
                            (uiState.isChallengeDismissible &&
                             !uiState.hasActiveModeEvent &&
                             !(uiState.isModeActive && uiState.dealType == GameDealType.MINI_GAME && uiState.miniGameResult != null))
                        }
                    ) {
                        val barEvent = uiState.barMode.activeEvent
                        if (barEvent != null && barEvent !is BarEvent.GiveDrinksPickTarget) onModeEventDismissed()
                        else onChallengeDismissed()
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

// ─── Truth or Dare challenge ──────────────────────────────────────────────────

@Composable
private fun TruthOrDareChallengeContent(
    uiState: GameScreenState,
    onTruthOrDareChosen: (TruthOrDareChoice) -> Unit,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlipCard(
        isFlipped = uiState.truthOrDareChoice != null,
        modifier = modifier,
        front = {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.truth_or_dare_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DealOptionButton(
                            text = stringResource(R.string.truth),
                            onClick = { onTruthOrDareChosen(TruthOrDareChoice.TRUTH) },
                            modifier = Modifier.weight(1f)
                        )
                        DealOptionButton(
                            text = stringResource(R.string.dare),
                            onClick = { onTruthOrDareChosen(TruthOrDareChoice.DARE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                uiState.selectedPlayer?.let { player ->
                    Text(
                        text = player.nickName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        },
        back = {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = uiState.truthOrDareChoice?.name.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = uiState.challengeText.orEmpty(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    if (uiState.isModeActive) {
                        DealOptionButton(
                            text = stringResource(R.string.skip),
                            onClick = onSkipped,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.tap_to_dismiss),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.selectedPlayer?.let { player ->
                    Text(
                        text = player.nickName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    )
}

// ─── Sticky dare challenge ────────────────────────────────────────────────────

@Composable
private fun StickyDareChallengeContent(
    uiState: GameScreenState,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.sticky_dare_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = uiState.challengeText.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
            if (uiState.isModeActive) {
                Spacer(Modifier.height(12.dp))
                DealOptionButton(
                    text = stringResource(R.string.skip),
                    onClick = onSkipped,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        uiState.selectedPlayer?.let { player ->
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

// ─── General knowledge challenge ─────────────────────────────────────────────

@Composable
private fun GeneralKnowledgeChallengeContent(
    uiState: GameScreenState,
    onAnswerSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState.generalKnowledgeQuestion ?: return
    val answered = uiState.selectedAnswerOption != null

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.general_knowledge_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnswerOptionButton(
                    text = question.optionA,
                    option = 'A',
                    selectedOption = uiState.selectedAnswerOption,
                    correctOption = question.correctOption,
                    onClick = { if (!answered) onAnswerSelected('A') },
                    modifier = Modifier.weight(1f)
                )
                AnswerOptionButton(
                    text = question.optionB,
                    option = 'B',
                    selectedOption = uiState.selectedAnswerOption,
                    correctOption = question.correctOption,
                    onClick = { if (!answered) onAnswerSelected('B') },
                    modifier = Modifier.weight(1f)
                )
            }
            if (answered) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        if (uiState.isModeActive) R.string.tap_to_continue else R.string.tap_to_dismiss
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
        uiState.selectedPlayer?.let { player ->
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

// ─── Mini-game challenge ──────────────────────────────────────────────────────

@Composable
private fun MiniGameChallengeContent(
    uiState: GameScreenState,
    onOpponentSelected: (Player) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val miniGame = uiState.miniGame ?: return
    val result = uiState.miniGameResult
    var pendingOpponent by remember { mutableStateOf<Player?>(null) }

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = if (result == null && pendingOpponent != null) 80.dp else 0.dp)
        ) {
            Text(
                text = stringResource(miniGame.nameRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            if (result == null) {
                // ── Opponent selection ──────────────────────────────────────
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.selectedPlayer?.nickName.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.choose_opponent),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                val opponents = uiState.players.filter { it != uiState.selectedPlayer }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((opponents.size * 64) + ((opponents.size - 1) * 8)).coerceAtMost(300).dp)
                ) {
                    items(opponents, key = { it.id }) { player ->
                        DealOptionButton(
                            text = player.nickName,
                            isSelected = player == pendingOpponent,
                            onClick = { pendingOpponent = player },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // ── Result summary ──────────────────────────────────────────
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (result.winner != null)
                        stringResource(R.string.mini_game_winner, result.winner!!)
                    else
                        stringResource(R.string.mini_game_tie),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${result.player1Name}:  ${result.player1Score}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${result.player2Name}:  ${result.player2Score}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                if (uiState.isModeActive) {
                    DealOptionButton(
                        text = stringResource(R.string.finish),
                        onClick = onFinished,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tap_to_dismiss),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (result == null && pendingOpponent != null) {
            DealOptionButton(
                text = stringResource(R.string.go),
                onClick = { onOpponentSelected(pendingOpponent!!) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

// ─── Shared button composables ────────────────────────────────────────────────

@Composable
private fun DealOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = if (isSelected) 0.4f else 0.2f),
            contentColor = Color.White
        ),
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnswerOptionButton(
    text: String,
    option: Char,
    selectedOption: Char?,
    correctOption: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        selectedOption == null -> Color.White.copy(alpha = 0.2f)
        option == correctOption -> Color(0xFF2E7D32).copy(alpha = 0.85f)
        option == selectedOption -> Color(0xFFC62828).copy(alpha = 0.85f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor,
            disabledContentColor = Color.White
        ),
        enabled = selectedOption == null,
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Mode event challenge content ─────────────────────────────────────────────

@Composable
private fun ModeEventChallengeContent(
    uiState: GameScreenState,
    onDismiss: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barEvent = uiState.barMode.activeEvent
    val couplesEvent = uiState.couplesMode.activeEvent

    if (barEvent == null && couplesEvent == null) return

    val titleRes = if (couplesEvent != null) R.string.couples_event_title else R.string.bar_event_title

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        when {
            barEvent is BarEvent.TakeDrinks -> DrinksFillIndicator(amount = barEvent.amount)
            barEvent != null -> Icon(
                painter = painterResource(R.drawable.ic_sports_bar),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            couplesEvent != null -> Image(
                painter = painterResource(R.drawable.img_couples_mode_illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        if (barEvent != null) {
            BarEventContent(
                event = barEvent,
                onDismiss = onDismiss,
                onGiveDrinksTargetSelected = onGiveDrinksTargetSelected
            )
        } else if (couplesEvent != null) {
            CouplesEventContent(event = couplesEvent, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun BarEventContent(
    event: BarEvent,
    onDismiss: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit
) {
    when (event) {
        is BarEvent.NoAction -> {
            Text(
                text = stringResource(R.string.bar_event_no_action),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
        is BarEvent.GiveDrinks -> {
            Text(
                text = stringResource(R.string.bar_event_give_drinks, event.amount, event.targetPlayerName),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
        is BarEvent.GiveDrinksPickTarget -> {
            Text(
                text = stringResource(R.string.bar_event_give_drinks_choose, event.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            event.candidates.forEach { name ->
                DealOptionButton(
                    text = name,
                    onClick = { onGiveDrinksTargetSelected(name) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        is BarEvent.TakeDrinks -> {
            Text(
                text = stringResource(R.string.bar_event_take_drinks, event.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CouplesEventContent(
    event: CouplesEvent,
    onDismiss: () -> Unit
) {
    val message = when (event) {
        is CouplesEvent.GiveAKiss -> stringResource(R.string.couples_event_give_a_kiss)
        is CouplesEvent.ChooseKissers -> stringResource(R.string.couples_event_chose_kissers)
        is CouplesEvent.MakeALoveDeclaration -> stringResource(R.string.couples_event_make_love_declaration, event.targetPlayerName)
        is CouplesEvent.ActOfLove -> stringResource(R.string.couples_event_act_of_love, event.requesterPlayerName)
        is CouplesEvent.ChooseLovers -> stringResource(R.string.couples_event_chose_lovers)
    }
    Text(
        text = message,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    DealOptionButton(
        text = stringResource(R.string.ok),
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    )
}

// ─── Flip card ────────────────────────────────────────────────────────────────

@Composable
private fun FlipCard(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "flip"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                back()
            }
        }
    }
}
