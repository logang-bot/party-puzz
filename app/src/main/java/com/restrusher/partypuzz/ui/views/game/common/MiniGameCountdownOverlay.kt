package com.restrusher.partypuzz.ui.views.game.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun MiniGameCountdownOverlay(
    countdownValue: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.16f)
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        // Top specular edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )
        // Bottom specular edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.30f))
                .align(Alignment.BottomCenter)
        )

        AnimatedContent(
            targetState = countdownValue,
            transitionSpec = {
                if (targetState == 0) {
                    (fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.5f)) togetherWith
                    (fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 1.5f))
                } else {
                    (slideInVertically(tween(220)) { -it } + fadeIn(tween(220))) togetherWith
                    (slideOutVertically(tween(220)) { it } + fadeOut(tween(220)))
                }
            },
            label = "countdown",
            contentAlignment = Alignment.Center
        ) { value ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (value > 0) stringResource(R.string.ready) else stringResource(R.string.go),
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.6f),
                            offset = Offset.Zero,
                            blurRadius = 24f
                        )
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (value > 0) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = Shadow(
                                color = Color.White.copy(alpha = 0.4f),
                                offset = Offset.Zero,
                                blurRadius = 16f
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun CountdownOverlayNumberPreview() {
    PartyPuzzTheme { MiniGameCountdownOverlay(countdownValue = 2) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun CountdownOverlayGoPreview() {
    PartyPuzzTheme { MiniGameCountdownOverlay(countdownValue = 0) }
}
