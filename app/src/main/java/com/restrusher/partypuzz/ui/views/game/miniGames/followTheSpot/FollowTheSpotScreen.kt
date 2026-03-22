package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun FollowTheSpotScreen(
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowTheSpotViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FollowTheSpotContent(
        uiState = uiState,
        onPlayer1SpotTapped = viewModel::onPlayer1SpotTapped,
        onPlayer2SpotTapped = viewModel::onPlayer2SpotTapped,
        onGameFinished = onGameFinished,
        modifier = modifier
    )
}

@Composable
internal fun FollowTheSpotContent(
    uiState: FollowTheSpotState,
    onPlayer1SpotTapped: () -> Unit,
    onPlayer2SpotTapped: () -> Unit,
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SpotBoard(
            spotNormX = uiState.player2SpotNormX,
            spotNormY = uiState.player2SpotNormY,
            isActive = uiState.isGameRunning,
            onSpotTapped = onPlayer2SpotTapped,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        GameDivider(
            player1 = uiState.player1,
            player2 = uiState.player2,
            player1Score = uiState.player1Score,
            player2Score = uiState.player2Score,
            timeRemaining = uiState.timeRemaining,
            isGameRunning = uiState.isGameRunning,
            onExitTapped = { onGameFinished(uiState.player1Score, uiState.player2Score) }
        )
        SpotBoard(
            spotNormX = uiState.player1SpotNormX,
            spotNormY = uiState.player1SpotNormY,
            isActive = uiState.isGameRunning,
            onSpotTapped = onPlayer1SpotTapped,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer1 = Player(id = 1, nickName = "Alice", gender = Gender.Female)
private val previewPlayer2 = Player(id = 2, nickName = "Bob", gender = Gender.Male)

@Preview(
    name = "Screen – game running",
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait"
)
@Composable
private fun FollowTheSpotContentRunningPreview() {
    PartyPuzzTheme {
        FollowTheSpotContent(
            uiState = FollowTheSpotState(
                player1 = previewPlayer1,
                player2 = previewPlayer2,
                player1Score = 3,
                player2Score = 5,
                timeRemaining = 8,
                player1SpotNormX = 0.3f,
                player1SpotNormY = 0.6f,
                player2SpotNormX = 0.7f,
                player2SpotNormY = 0.35f,
                isGameRunning = true
            ),
            onPlayer1SpotTapped = {},
            onPlayer2SpotTapped = {},
            onGameFinished = { _, _ -> }
        )
    }
}

@Preview(
    name = "Screen – game over",
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait"
)
@Composable
private fun FollowTheSpotContentFinishedPreview() {
    PartyPuzzTheme {
        FollowTheSpotContent(
            uiState = FollowTheSpotState(
                player1 = previewPlayer1,
                player2 = previewPlayer2,
                player1Score = 7,
                player2Score = 4,
                timeRemaining = 0,
                isGameRunning = false
            ),
            onPlayer1SpotTapped = {},
            onPlayer2SpotTapped = {},
            onGameFinished = { _, _ -> }
        )
    }
}
