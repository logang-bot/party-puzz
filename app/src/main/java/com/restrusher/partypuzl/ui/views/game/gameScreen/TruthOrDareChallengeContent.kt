package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink

/**
 * The truth or dare prompt. The Truth / Dare split is decided in the deal picker, so this only
 * ever renders the prompt the player already committed to.
 */
@Composable
internal fun TruthOrDareChallengeContent(
    uiState: GameScreenState,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = uiState.truthOrDareChoice?.accent ?: dareAccent

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = stringResource(accent.labelRes).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 3.sp,
                color = accent.tone,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = uiState.challengeText.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
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
                    color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary),
                    textAlign = TextAlign.Center
                )
            }
        }
        uiState.selectedPlayer?.let { player ->
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Strong),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

private val previewPlayer = Player(1, "Alice", Gender.Female, InterestedIn.Man)

@Preview(name = "TruthOrDare – truth – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun TruthOrDareTruthLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize()) {
            TruthOrDareChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer,
                    truthOrDareChoice = TruthOrDareChoice.TRUTH,
                    challengeText = "What is the pettiest reason you have ever ended a friendship?"
                ),
                onSkipped = {}
            )
        }
    }
}

@Preview(name = "TruthOrDare – dare – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun TruthOrDareDareDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize()) {
            TruthOrDareChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer,
                    truthOrDareChoice = TruthOrDareChoice.DARE,
                    challengeText = "Do a handstand for 10 seconds",
                    barMode = BarModeState(isActive = true)
                ),
                onSkipped = {}
            )
        }
    }
}
