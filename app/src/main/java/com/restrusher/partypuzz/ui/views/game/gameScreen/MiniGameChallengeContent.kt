package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
internal fun MiniGameChallengeContent(
    uiState: GameScreenState,
    onOpponentSelected: (Player) -> Unit,
    onGlobalMiniGameStarted: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val miniGame = uiState.miniGame ?: return
    val result = uiState.miniGameResult
    var pendingOpponent by remember { mutableStateOf<Player?>(null) }

    val bottomButtonVisible = result == null &&
            (miniGame.isGlobal || pendingOpponent != null)
    val useLargeHeader = result == null && miniGame.isGlobal

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = if (bottomButtonVisible) 80.dp else 0.dp)
        ) {
            MiniGameHeader(miniGame = miniGame, isLarge = useLargeHeader)
            when (result) {
                is ScoredMiniGameResult -> ScoredResultContent(
                    result = result,
                    isModeActive = uiState.isModeActive,
                    onFinished = onFinished
                )
                is LoserMiniGameResult -> LoserResultContent(
                    result = result,
                    isModeActive = uiState.isModeActive,
                    onFinished = onFinished
                )
                null -> if (miniGame.isGlobal) {
                    GlobalMiniGameContent(miniGame = miniGame)
                } else {
                    OpponentSelectionContent(
                        miniGame = miniGame,
                        uiState = uiState,
                        pendingOpponent = pendingOpponent,
                        onPendingOpponentChanged = { pendingOpponent = it }
                    )
                }
            }
        }
        if (result == null) {
            when {
                miniGame.isGlobal -> DealOptionButton(
                    text = stringResource(R.string.start),
                    onClick = onGlobalMiniGameStarted,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                )
                pendingOpponent != null -> DealOptionButton(
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
}

@Composable
private fun MiniGameHeader(miniGame: MiniGame, isLarge: Boolean) {
    if (isLarge) {
        Text(
            text = stringResource(miniGame.nameRes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        if (miniGame.isGlobal) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.mini_game_everyone_plays),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Text(
            text = stringResource(miniGame.nameRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GlobalMiniGameContent(miniGame: MiniGame) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(miniGame.descriptionRes),
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.80f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(80.dp))
}

@Composable
private fun OpponentSelectionContent(
    miniGame: MiniGame,
    uiState: GameScreenState,
    pendingOpponent: Player?,
    onPendingOpponentChanged: (Player) -> Unit
) {
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(miniGame.descriptionRes),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.75f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = uiState.selectedPlayer?.nickName.orEmpty(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
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
                onClick = { onPendingOpponentChanged(player) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScoredResultContent(
    result: ScoredMiniGameResult,
    isModeActive: Boolean,
    onFinished: () -> Unit
) {
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
    ResultDismissAction(isModeActive = isModeActive, onFinished = onFinished)
}

@Composable
private fun LoserResultContent(
    result: LoserMiniGameResult,
    isModeActive: Boolean,
    onFinished: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.mini_game_loser, result.loserName),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    ResultDismissAction(isModeActive = isModeActive, onFinished = onFinished)
}

@Composable
private fun ResultDismissAction(isModeActive: Boolean, onFinished: () -> Unit) {
    if (isModeActive) {
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

private val previewPlayer1 = Player(1, "Alice", Gender.Female, InterestedIn.Man)
private val previewPlayer2 = Player(2, "Bob", Gender.Male, InterestedIn.Woman)
private val previewPlayer3 = Player(3, "Carol", Gender.Female, InterestedIn.Both)

@Preview(name = "MiniGame – opponent selection – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun MiniGameSelectionLightPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            MiniGameChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer1,
                    players = listOf(previewPlayer1, previewPlayer2, previewPlayer3),
                    miniGame = MiniGame.FOLLOW_THE_SPOT
                ),
                onGlobalMiniGameStarted = {},
                onOpponentSelected = {},
                onFinished = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "MiniGame – result – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun MiniGameResultDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            MiniGameChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer1,
                    players = listOf(previewPlayer1, previewPlayer2),
                    miniGame = MiniGame.FOLLOW_THE_SPOT,
                    miniGameResult = ScoredMiniGameResult(
                        player1Name = "Alice",
                        player1Score = 3,
                        player2Name = "Bob",
                        player2Score = 1
                    )
                ),
                onGlobalMiniGameStarted = {},
                onOpponentSelected = {},
                onFinished = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "MiniGame – hot potato loser – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun MiniGameHotPotatoLoserDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            MiniGameChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer1,
                    players = listOf(previewPlayer1, previewPlayer2, previewPlayer3),
                    miniGame = MiniGame.HOT_POTATO,
                    miniGameResult = LoserMiniGameResult(loserName = "Carol")
                ),
                onGlobalMiniGameStarted = {},
                onOpponentSelected = {},
                onFinished = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
