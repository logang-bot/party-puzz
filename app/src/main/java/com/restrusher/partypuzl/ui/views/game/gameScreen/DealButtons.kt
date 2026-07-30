package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AnswerCorrectGreen
import com.restrusher.partypuzl.ui.theme.AnswerWrongRed
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

private val correctAnswerColor = AnswerCorrectGreen
private val wrongAnswerColor = AnswerWrongRed

@Composable
internal fun DealOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = with(MaterialTheme.appColors) {
                if (isSelected) panelFillSelected else panelFillRaised
            },
            contentColor = MaterialTheme.colorScheme.onBackground
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
internal fun AnswerOptionButton(
    text: String,
    option: Char,
    selectedOption: Char?,
    correctOption: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnswered = selectedOption != null
    val isCorrect = option == correctOption
    val isWrongPick = isAnswered && option == selectedOption && !isCorrect

    val containerColor = when {
        !isAnswered -> MaterialTheme.colorScheme.onBackground.wash(Wash.Fill)
        isCorrect -> correctAnswerColor.ink(Ink.Strong)
        isWrongPick -> wrongAnswerColor.ink(Ink.Strong)
        else -> MaterialTheme.colorScheme.onBackground.wash(Wash.Faint)
    }
    val contentColor = when {
        isAnswered && (isCorrect || isWrongPick) -> MaterialTheme.appColors.onAccentSurface
        else -> MaterialTheme.colorScheme.onBackground
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        enabled = !isAnswered,
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

@Preview(name = "DealOptionButton – Light", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun DealOptionButtonLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DealOptionButton(text = "Truth", onClick = {}, modifier = Modifier.weight(1f))
                DealOptionButton(text = "Dare", onClick = {}, isSelected = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(name = "AnswerOptionButton – Dark", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun AnswerOptionButtonDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnswerOptionButton(
                    text = "Paris",
                    option = 'A',
                    selectedOption = 'A',
                    correctOption = 'B',
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                AnswerOptionButton(
                    text = "London",
                    option = 'B',
                    selectedOption = 'A',
                    correctOption = 'B',
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(name = "DealOptionButton – Dark – selected", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun DealOptionButtonSelectedDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().padding(16.dp)) {
            DealOptionButton(
                text = "Skip",
                onClick = {},
                isSelected = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
