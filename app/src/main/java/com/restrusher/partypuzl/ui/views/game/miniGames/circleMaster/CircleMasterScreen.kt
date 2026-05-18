package com.restrusher.partypuzl.ui.views.game.miniGames.circleMaster

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.common.BackPressToExit
import com.restrusher.partypuzl.ui.common.LockScreenOrientation
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.views.game.common.MiniGameCountdownOverlay
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

@Composable
fun CircleMasterScreen(
    onGameFinished: (loserName: String) -> Unit,
    onAbortGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CircleMasterViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackPressToExit(
        warningMessage = stringResource(R.string.press_back_again_to_exit),
        onExit = onAbortGame
    )
    CircleMasterContent(
        uiState = uiState,
        onDrawPoint = viewModel::onDrawPoint,
        onDrawEnd = viewModel::onDrawEnd,
        onScoreAcknowledged = viewModel::onScoreAcknowledged,
        onPassConfirmed = viewModel::onPassConfirmed,
        onDone = { onGameFinished(uiState.loser?.nickName.orEmpty()) },
        modifier = modifier
    )
}

@Composable
internal fun CircleMasterContent(
    uiState: CircleMasterState,
    onDrawPoint: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onScoreAcknowledged: () -> Unit,
    onPassConfirmed: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScoreReveal = uiState.phase == CircleMasterPhase.SCORE_REVEAL
    val isWinner = uiState.phase == CircleMasterPhase.WINNER
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isScoreReveal || isWinner,
                onClick = { if (isScoreReveal) onScoreAcknowledged() else onDone() }
            )
    ) {
        val contentModifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
        when (uiState.phase) {
            CircleMasterPhase.COUNTDOWN,
            CircleMasterPhase.DRAWING,
            CircleMasterPhase.SCORE_REVEAL -> DrawingContent(
                uiState = uiState,
                onDrawPoint = onDrawPoint,
                onDrawEnd = onDrawEnd,
                modifier = contentModifier
            )
            CircleMasterPhase.PASS -> PassContent(
                nextPlayer = uiState.nextPlayer,
                onPassConfirmed = onPassConfirmed,
                modifier = contentModifier
            )
            CircleMasterPhase.WINNER -> WinnerContent(
                players = uiState.players,
                scores = uiState.scores,
                winner = uiState.winner,
                modifier = contentModifier
            )
        }
        AnimatedVisibility(
            visible = uiState.phase == CircleMasterPhase.COUNTDOWN,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            MiniGameCountdownOverlay(countdownValue = uiState.countdownValue)
        }
    }
}

@Composable
private fun DrawingContent(
    uiState: CircleMasterState,
    onDrawPoint: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentBlur by animateDpAsState(
        targetValue = if (uiState.phase == CircleMasterPhase.COUNTDOWN) 16.dp else 0.dp,
        animationSpec = tween(400),
        label = "blur"
    )
    val scoreVisible = uiState.phase == CircleMasterPhase.SCORE_REVEAL && uiState.currentScore != null
    Column(modifier = modifier.blur(contentBlur), horizontalAlignment = Alignment.CenterHorizontally) {
        PlayerTurnHeader(player = uiState.currentPlayer, modifier = Modifier.padding(16.dp))
        CircleCanvas(
            drawnPath = uiState.drawnPath,
            isDrawingEnabled = uiState.phase == CircleMasterPhase.DRAWING,
            onDrawPoint = onDrawPoint,
            onDrawEnd = onDrawEnd,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
        )
        // Fixed-height footer: both variants are always laid out so the canvas never resizes.
        // Alpha hides the inactive one without removing it from the layout.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            ScoreRevealFooter(
                score = uiState.currentScore ?: 0,
                modifier = Modifier.alpha(if (scoreVisible) 1f else 0f)
            )
            Text(
                text = stringResource(R.string.circle_master_draw_instruction),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(if (scoreVisible) 0f else 1f)
            )
        }
    }
}

@Composable
private fun PlayerTurnHeader(player: Player?, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape)) {
            if (player != null) PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = player?.nickName?.substringBefore(" ").orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ScoreRevealFooter(score: Int, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(scoreLabelRes(score)),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.tap_to_dismiss),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PassContent(nextPlayer: Player?, onPassConfirmed: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onPassConfirmed
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape)) {
                if (nextPlayer != null) PlayerPhoto(player = nextPlayer, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.circle_master_pass_to, nextPlayer?.nickName?.substringBefore(" ").orEmpty()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun WinnerContent(
    players: List<Player>,
    scores: Map<String, Int>,
    winner: Player?,
    modifier: Modifier = Modifier
) {
    val sorted = players.sortedByDescending { scores[it.nickName] ?: 0 }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.circle_master_results),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))
        sorted.forEach { player ->
            val score = scores[player.nickName] ?: 0
            val isWinner = player.nickName == winner?.nickName
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape)) {
                    PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = player.nickName.substringBefore(" "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.tap_to_exit),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@StringRes
private fun scoreLabelRes(score: Int): Int = when {
    score >= 90 -> R.string.circle_master_score_perfect
    score >= 75 -> R.string.circle_master_score_great
    score >= 60 -> R.string.circle_master_score_not_bad
    score >= 40 -> R.string.circle_master_score_try_harder
    else -> R.string.circle_master_score_oops
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bob", Gender.Male, InterestedIn.Woman),
)

@Preview(name = "Drawing – Light", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420")
@Preview(name = "Drawing – Dark", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrawingPreview() {
    PartyPuzlTheme {
        CircleMasterContent(
            uiState = CircleMasterState(players = previewPlayers, phase = CircleMasterPhase.DRAWING),
            onDrawPoint = {}, onDrawEnd = {}, onScoreAcknowledged = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}

@Preview(name = "Score Reveal – Light", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420")
@Composable
private fun ScoreRevealPreview() {
    PartyPuzlTheme {
        CircleMasterContent(
            uiState = CircleMasterState(players = previewPlayers, phase = CircleMasterPhase.SCORE_REVEAL, currentScore = 78),
            onDrawPoint = {}, onDrawEnd = {}, onScoreAcknowledged = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}

@Preview(name = "Winner – Light", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420")
@Composable
private fun WinnerPreview() {
    PartyPuzlTheme {
        CircleMasterContent(
            uiState = CircleMasterState(
                players = previewPlayers,
                phase = CircleMasterPhase.WINNER,
                scores = mapOf("Alice" to 82, "Bob" to 55),
                winner = previewPlayers[0],
                loser = previewPlayers[1]
            ),
            onDrawPoint = {}, onDrawEnd = {}, onScoreAcknowledged = {}, onPassConfirmed = {}, onDone = {}
        )
    }
}
