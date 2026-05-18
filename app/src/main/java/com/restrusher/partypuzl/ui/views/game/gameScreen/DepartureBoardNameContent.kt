package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val flipChars = ('A'..'Z').toList()
private const val BOARD_WIDTH = 6

@Composable
internal fun DepartureBoardNameContent(
    modifier: Modifier = Modifier
) {
    val displayChars = remember { List(BOARD_WIDTH) { mutableStateOf(flipChars.random()) } }

    LaunchedEffect(Unit) {
        displayChars.indices.forEach { i ->
            launch {
                while (true) {
                    displayChars[i].value = flipChars.random()
                    delay(120L + i * 20L)
                }
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        displayChars.forEach { charState ->
            Text(
                text = charState.value.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
