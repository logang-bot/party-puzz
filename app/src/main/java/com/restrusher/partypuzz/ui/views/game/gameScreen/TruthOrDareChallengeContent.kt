package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
internal fun TruthOrDareChallengeContent(
    uiState: GameScreenState,
    onTruthOrDareChosen: (TruthOrDareChoice) -> Unit,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlipCard(
        isFlipped = uiState.truthOrDareChoice != null,
        modifier = modifier,
        front = { TruthOrDareFront(uiState = uiState, onTruthOrDareChosen = onTruthOrDareChosen) },
        back = { TruthOrDareBack(uiState = uiState, onSkipped = onSkipped) }
    )
}

@Composable
private fun TruthOrDareFront(
    uiState: GameScreenState,
    onTruthOrDareChosen: (TruthOrDareChoice) -> Unit
) {
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
}

@Composable
private fun TruthOrDareBack(
    uiState: GameScreenState,
    onSkipped: () -> Unit
) {
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

private val previewPlayer = Player(1, "Alice", Gender.Female, InterestedIn.Man)

@Preview(name = "TruthOrDare – front – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun TruthOrDareFrontLightPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            TruthOrDareChallengeContent(
                uiState = GameScreenState(selectedPlayer = previewPlayer),
                onTruthOrDareChosen = {},
                onSkipped = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "TruthOrDare – back – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun TruthOrDareBackDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            TruthOrDareChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer,
                    truthOrDareChoice = TruthOrDareChoice.DARE,
                    challengeText = "Do a handstand for 10 seconds"
                ),
                onTruthOrDareChosen = {},
                onSkipped = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
