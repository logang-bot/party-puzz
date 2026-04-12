package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
internal fun FlipCard(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "flip"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                back()
            }
        }
    }
}

@Preview(name = "FlipCard – front – Light", showBackground = true, widthDp = 300, heightDp = 400)
@Composable
private fun FlipCardFrontLightPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        FlipCard(
            isFlipped = false,
            modifier = Modifier.fillMaxSize(),
            front = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Front") } },
            back = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Back") } }
        )
    }
}

@Preview(name = "FlipCard – back – Dark", showBackground = true, widthDp = 300, heightDp = 400)
@Composable
private fun FlipCardBackDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        FlipCard(
            isFlipped = true,
            modifier = Modifier.fillMaxSize(),
            front = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Front") } },
            back = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Back") } }
        )
    }
}
