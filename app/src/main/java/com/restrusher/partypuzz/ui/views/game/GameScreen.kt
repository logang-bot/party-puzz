package com.restrusher.partypuzz.ui.views.game

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import java.io.File

// ─── Theming constants ────────────────────────────────────────────────────────

private val backgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1B1B2F), Color(0xFF162447), Color(0xFF1F4068))
)

private val glassBrush = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
)

private val glassCardShape = RoundedCornerShape(24.dp)
private val playerCardShape = RoundedCornerShape(12.dp)

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

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameScreenViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(id = R.string.exit_game_title)) },
            text = { Text(text = stringResource(id = R.string.exit_game_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) { Text(text = stringResource(id = R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_door_back),
                    contentDescription = stringResource(id = R.string.exit_game_title),
                    tint = Color.White
                )
            }
            GameDealSection(
                uiState = uiState,
                onGameDealTapped = viewModel::onGameDealTapped,
                onChallengeDismissed = viewModel::onChallengeDismissed,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            PlayersListRow(
                players = uiState.players,
                selectedPlayer = uiState.selectedPlayer,
                dealPhase = uiState.dealPhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}

// ─── Game deal section ────────────────────────────────────────────────────────

@Composable
private fun GameDealSection(
    uiState: GameScreenState,
    onGameDealTapped: () -> Unit,
    onChallengeDismissed: () -> Unit,
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
                        indication = null
                    ) { onChallengeDismissed() }
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

                Text(
                    text = "test",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                )
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
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                CardContent.CYCLING_NAMES -> AnimatedContent(
                    targetState = uiState.animatingName,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "cycling name"
                ) { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                CardContent.PLAYER_NAME -> uiState.selectedPlayer?.let { player ->
                    Text(
                        text = player.nickName,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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

// ─── Players list row ─────────────────────────────────────────────────────────

@Composable
private fun PlayersListRow(
    players: List<Player>,
    selectedPlayer: Player?,
    dealPhase: GameDealPhase,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(dealPhase) {
        if (dealPhase == GameDealPhase.PLAYER_NAME_REVEAL) {
            val index = selectedPlayer?.let { players.indexOf(it) } ?: return@LaunchedEffect
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    val isHighlightActive = dealPhase != GameDealPhase.IDLE && dealPhase != GameDealPhase.ANIMATING

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(players, key = { it.id }) { player ->
            PlayerAvatarCard(
                player = player,
                isHighlighted = isHighlightActive && player == selectedPlayer,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(64.dp)
                    .fillMaxHeight()
            )
        }
    }
}

// ─── Player avatar card (photo only) ─────────────────────────────────────────

@Composable
private fun PlayerAvatarCard(
    player: Player,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "highlight border"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = playerCardShape,
        modifier = modifier.border(2.dp, borderColor, playerCardShape)
    ) {
        PlayerPhoto(
            player = player,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ─── Shared photo renderer ────────────────────────────────────────────────────

@Composable
private fun PlayerPhoto(
    player: Player,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    when {
        player.photoPath != null -> AsyncImage(
            model = ImageRequest.Builder(context).data(File(player.photoPath)).build(),
            contentDescription = stringResource(id = R.string.player_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        player.avatarName != null -> {
            val resId = context.resources.getIdentifier(
                player.avatarName, "drawable", context.packageName
            )
            Image(
                painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                contentDescription = stringResource(id = R.string.player_avatar),
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> Image(
            painter = painterResource(id = R.drawable.img_dummy_avatar),
            contentDescription = stringResource(id = R.string.player_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
