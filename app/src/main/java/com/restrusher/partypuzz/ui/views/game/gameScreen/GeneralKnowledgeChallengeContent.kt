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
internal fun GeneralKnowledgeChallengeContent(
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

private val previewPlayer = Player(1, "Carlos", Gender.Male, InterestedIn.Both)
private val previewQuestion = GeneralKnowledgeQuestion(
    question = "What is the capital of France?",
    optionA = "Berlin",
    optionB = "Paris",
    correctOption = 'B'
)

@Preview(name = "GeneralKnowledge – unanswered – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun GeneralKnowledgeUnansweredLightPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            GeneralKnowledgeChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer,
                    generalKnowledgeQuestion = previewQuestion
                ),
                onAnswerSelected = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "GeneralKnowledge – answered – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun GeneralKnowledgeAnsweredDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            GeneralKnowledgeChallengeContent(
                uiState = GameScreenState(
                    selectedPlayer = previewPlayer,
                    generalKnowledgeQuestion = previewQuestion,
                    selectedAnswerOption = 'A'
                ),
                onAnswerSelected = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
