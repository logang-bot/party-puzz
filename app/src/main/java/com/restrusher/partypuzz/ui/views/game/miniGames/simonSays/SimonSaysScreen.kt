package com.restrusher.partypuzz.ui.views.game.miniGames.simonSays

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.BackPressToExit
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.ui.views.game.common.MiniGameCountdownOverlay
import com.restrusher.partypuzz.ui.views.game.common.PlayerPhoto

@Composable
fun SimonSaysScreen(
    onGameFinished: (loserName: String) -> Unit,
    onAbortGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SimonSaysViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackPressToExit(
        warningMessage = stringResource(R.string.press_back_again_to_exit),
        onExit = onAbortGame
    )
    SimonSaysContent(
        uiState = uiState,
        onButtonTapped = viewModel::onButtonTapped,
        onPassConfirmed = viewModel::onPassConfirmed,
        onDone = { onGameFinished(uiState.loser?.nickName.orEmpty()) },
        modifier = modifier
    )
}

@Composable
internal fun SimonSaysContent(
    uiState: SimonSaysState,
    onButtonTapped: (Int) -> Unit,
    onPassConfirmed: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentBlur by animateDpAsState(
        targetValue = if (uiState.phase == SimonSaysPhase.COUNTDOWN) 16.dp else 0.dp,
        animationSpec = tween(400), label = "blur"
    )
    val isGameOver = uiState.phase == SimonSaysPhase.GAME_OVER
    val gameOverInteractionSource = remember { MutableInteractionSource() }
    val gameOverAlpha by animateFloatAsState(
        targetValue = if (isGameOver) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = if (isGameOver) 300 else 0),
        label = "gameOverAlpha"
    )
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().blur(contentBlur)
        ) {
            RoundHeader(
                roundNumber = uiState.roundNumber,
                playerInputIndex = uiState.playerInputIndex,
                isInputPhase = uiState.phase == SimonSaysPhase.INPUT
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                SimonGrid(
                    highlightedButton = uiState.highlightedButton,
                    isInputPhase = uiState.phase == SimonSaysPhase.INPUT,
                    isGameOver = uiState.phase == SimonSaysPhase.GAME_OVER,
                    onButtonTapped = onButtonTapped,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(gameOverAlpha)
                        .then(
                            if (isGameOver) Modifier.clickable(
                                interactionSource = gameOverInteractionSource,
                                indication = null,
                                onClick = onDone
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tap_to_exit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            PhaseFooter(uiState = uiState, onPassConfirmed = onPassConfirmed)
        }
        AnimatedVisibility(
            visible = uiState.phase == SimonSaysPhase.COUNTDOWN,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            MiniGameCountdownOverlay(countdownValue = uiState.countdownValue, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun RoundHeader(
    roundNumber: Int,
    playerInputIndex: Int,
    isInputPhase: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(if (roundNumber == 0) 0f else 1f)
        ) {
            Text(
                text = stringResource(R.string.simon_says_round, maxOf(1, roundNumber)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isInputPhase) {
                Text(
                    text = "  •  ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.simon_says_step, playerInputIndex, roundNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlayerInfo(
    player: Player?,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape)) {
            if (player != null) PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SimonGrid(
    highlightedButton: Int,
    isInputPhase: Boolean,
    isGameOver: Boolean,
    onButtonTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SimonButton(0, highlightedButton == 0, isInputPhase, { onButtonTapped(0) }, isGameOver, Modifier.weight(1f).fillMaxHeight())
            SimonButton(1, highlightedButton == 1, isInputPhase, { onButtonTapped(1) }, isGameOver, Modifier.weight(1f).fillMaxHeight())
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SimonButton(2, highlightedButton == 2, isInputPhase, { onButtonTapped(2) }, isGameOver, Modifier.weight(1f).fillMaxHeight())
            SimonButton(3, highlightedButton == 3, isInputPhase, { onButtonTapped(3) }, isGameOver, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun PhaseFooter(
    uiState: SimonSaysState,
    onPassConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerName = uiState.currentPlayer?.nickName?.substringBefore(" ").orEmpty()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = uiState.phase == SimonSaysPhase.PASS,
                onClick = onPassConfirmed
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState.phase) {
            SimonSaysPhase.SHOWING -> PlayerInfo(
                player = uiState.currentPlayer,
                message = stringResource(R.string.simon_says_watch_player, playerName)
            )
            SimonSaysPhase.INPUT -> PlayerInfo(
                player = uiState.currentPlayer,
                message = stringResource(R.string.simon_says_now_your_turn, playerName)
            )
            SimonSaysPhase.PASS -> PlayerInfo(
                player = uiState.currentPlayer,
                message = stringResource(R.string.simon_says_pass_complete, playerName),
            )
            SimonSaysPhase.GAME_OVER -> PlayerInfo(
                player = uiState.loser,
                message = stringResource(R.string.simon_says_missed, uiState.loser?.nickName?.substringBefore(" ").orEmpty())
            )
            SimonSaysPhase.COUNTDOWN -> Box(modifier = Modifier.alpha(0f)) {
                PlayerInfo(
                    player = uiState.currentPlayer,
                    message = stringResource(R.string.simon_says_watch_player, playerName)
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bob", Gender.Male, InterestedIn.Woman),
)

@Preview(name = "SimonSays – showing (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "SimonSays – showing (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonSaysShowingPreview() {
    PartyPuzzTheme {
        SimonSaysContent(
            uiState = SimonSaysState(players = previewPlayers, sequence = listOf(0, 2, 1), phase = SimonSaysPhase.SHOWING, highlightedButton = 2),
            onButtonTapped = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}

@Preview(name = "SimonSays – input (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "SimonSays – input (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonSaysInputPreview() {
    PartyPuzzTheme {
        SimonSaysContent(
            uiState = SimonSaysState(players = previewPlayers, sequence = listOf(0, 2, 1), phase = SimonSaysPhase.INPUT, playerInputIndex = 1),
            onButtonTapped = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}

@Preview(name = "SimonSays – pass (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "SimonSays – pass (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonSaysPassPreview() {
    PartyPuzzTheme {
        SimonSaysContent(
            uiState = SimonSaysState(players = previewPlayers, sequence = listOf(0, 2, 1), phase = SimonSaysPhase.PASS, currentPlayerIndex = 1),
            onButtonTapped = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}

@Preview(name = "SimonSays – game over (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "SimonSays – game over (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonSaysGameOverPreview() {
    PartyPuzzTheme {
        SimonSaysContent(
            uiState = SimonSaysState(players = previewPlayers, sequence = listOf(0, 2, 1), phase = SimonSaysPhase.GAME_OVER, loser = previewPlayers[0]),
            onButtonTapped = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}
