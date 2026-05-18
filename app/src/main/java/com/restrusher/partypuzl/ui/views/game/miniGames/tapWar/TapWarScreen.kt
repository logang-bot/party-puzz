package com.restrusher.partypuzl.ui.views.game.miniGames.tapWar

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
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

@Composable
fun TapWarScreen(
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit,
    onAbortGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TapWarViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackPressToExit(
        warningMessage = stringResource(R.string.press_back_again_to_exit),
        onExit = onAbortGame
    )
    TapWarContent(
        uiState = uiState,
        onPlayer1Tapped = viewModel::onPlayer1Tapped,
        onPlayer2Tapped = viewModel::onPlayer2Tapped,
        onGameFinished = onGameFinished,
        modifier = modifier
    )
}

@Composable
internal fun TapWarContent(
    uiState: TapWarState,
    onPlayer1Tapped: () -> Unit,
    onPlayer2Tapped: () -> Unit,
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentBlur by animateDpAsState(
        targetValue = if (uiState.isCountingDown) 16.dp else 0.dp,
        animationSpec = tween(400),
        label = "contentBlur"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .blur(contentBlur)
        ) {
            TapWarSide(
                player = uiState.player2,
                isActive = uiState.isGameRunning,
                dividerEdge = TapWarDividerEdge.Bottom,
                isFlipped = true,
                onTapped = onPlayer2Tapped,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            TugOfWarBar(
                player1 = uiState.player1,
                player2 = uiState.player2,
                barPosition = uiState.barPosition,
                isGameRunning = uiState.isGameRunning,
                timeRemaining = uiState.timeRemaining,
                totalDuration = TapWarViewModel.GAME_DURATION_SECONDS,
                onExitTapped = {
                    val p1Score = if (uiState.winner == 1) 1 else 0
                    val p2Score = if (uiState.winner == 2) 1 else 0
                    onGameFinished(p1Score, p2Score)
                }
            )
            TapWarSide(
                player = uiState.player1,
                isActive = uiState.isGameRunning,
                dividerEdge = TapWarDividerEdge.Top,
                isFlipped = false,
                onTapped = onPlayer1Tapped,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = uiState.isCountingDown,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            MiniGameCountdownOverlay(
                countdownValue = uiState.countdownValue,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer1 = Player(id = 1, nickName = "Alice", gender = Gender.Female, interestedIn = InterestedIn.Man)
private val previewPlayer2 = Player(id = 2, nickName = "Bob", gender = Gender.Male, interestedIn = InterestedIn.Woman)

@Preview(name = "TapWarContent – countdown (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "TapWarContent – countdown (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TapWarContentCountdownPreview() {
    PartyPuzlTheme {
        TapWarContent(
            uiState = TapWarState(player1 = previewPlayer1, player2 = previewPlayer2, isCountingDown = true, countdownValue = 2),
            onPlayer1Tapped = {}, onPlayer2Tapped = {}, onGameFinished = { _, _ -> }
        )
    }
}

@Preview(name = "TapWarContent – running (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "TapWarContent – running (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TapWarContentRunningPreview() {
    PartyPuzlTheme {
        TapWarContent(
            uiState = TapWarState(player1 = previewPlayer1, player2 = previewPlayer2, barPosition = 0.65f, isGameRunning = true, timeRemaining = 7),
            onPlayer1Tapped = {}, onPlayer2Tapped = {}, onGameFinished = { _, _ -> }
        )
    }
}

@Preview(name = "TapWarContent – game over (Light)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Preview(name = "TapWarContent – game over (Dark)", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TapWarContentFinishedPreview() {
    PartyPuzlTheme {
        TapWarContent(
            uiState = TapWarState(player1 = previewPlayer1, player2 = previewPlayer2, barPosition = 1f, isGameRunning = false, winner = 1),
            onPlayer1Tapped = {}, onPlayer2Tapped = {}, onGameFinished = { _, _ -> }
        )
    }
}
