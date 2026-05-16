package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val scrambleChars = ('A'..'Z').toList()

@Composable
internal fun ScrambledNameContent(
    names: List<String>,
    modifier: Modifier = Modifier
) {
    if (names.isEmpty()) return

    // Shuffle once per deal so the cycling order feels random each time.
    val shuffled = remember { names.shuffled() }

    var displayText by remember {
        mutableStateOf(shuffled.first().map { scrambleChars.random() }.joinToString(""))
    }

    LaunchedEffect(Unit) {
        var nameIndex = 0
        while (true) {
            val name = shuffled[nameIndex]

            // Scramble: random chars at the same length as the real name
            repeat(5) {
                displayText = name.map { scrambleChars.random() }.joinToString("")
                delay(50L)
            }

            // Reveal: lock characters left-to-right
            val revealPerChar = 300L / name.length.coerceAtLeast(1)
            for (locked in 1..name.length) {
                displayText = name.mapIndexed { i, c ->
                    if (i < locked) c else scrambleChars.random()
                }.joinToString("")
                delay(revealPerChar)
            }

            // Hold: show the full real name briefly
            displayText = name
            delay(300L)

            nameIndex = (nameIndex + 1) % shuffled.size
        }
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    )
}
