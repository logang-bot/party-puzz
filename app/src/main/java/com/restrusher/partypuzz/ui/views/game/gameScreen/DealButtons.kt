package com.restrusher.partypuzz.ui.views.game.gameScreen

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
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

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
            containerColor = Color.White.copy(alpha = if (isSelected) 0.4f else 0.2f),
            contentColor = Color.White
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
    val containerColor = when {
        selectedOption == null -> Color.White.copy(alpha = 0.2f)
        option == correctOption -> Color(0xFF2E7D32).copy(alpha = 0.85f)
        option == selectedOption -> Color(0xFFC62828).copy(alpha = 0.85f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor,
            disabledContentColor = Color.White
        ),
        enabled = selectedOption == null,
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
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFF162447)).padding(16.dp)) {
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
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).padding(16.dp)) {
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
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF162447)).padding(16.dp)) {
            DealOptionButton(
                text = "Skip",
                onClick = {},
                isSelected = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
