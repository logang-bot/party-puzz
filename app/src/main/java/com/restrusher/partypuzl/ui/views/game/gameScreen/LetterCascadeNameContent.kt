package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
internal fun LetterCascadeNameContent(
    names: List<String>,
    loop: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (names.isEmpty()) return

    val firstWords = remember { names.shuffled().map { it.substringBefore(' ') } }
    var nameIndex by remember { mutableIntStateOf(0) }
    val currentWord = firstWords[nameIndex]

    val letterYOffsets = remember(nameIndex) { List(currentWord.length) { Animatable(-200f) } }
    val wordAlpha = remember(nameIndex) { Animatable(1f) }

    LaunchedEffect(nameIndex) {
        val dropJobs = currentWord.indices.map { i ->
            launch {
                delay(i * 120L)
                letterYOffsets[i].animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
        dropJobs.joinAll()
        if (!loop) return@LaunchedEffect
        delay(500L)
        wordAlpha.animateTo(0f, animationSpec = tween(200))
        nameIndex = (nameIndex + 1) % firstWords.size
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        currentWord.forEachIndexed { i, char ->
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.graphicsLayer {
                    translationY = letterYOffsets[i].value
                    alpha = wordAlpha.value
                }
            )
        }
    }
}
