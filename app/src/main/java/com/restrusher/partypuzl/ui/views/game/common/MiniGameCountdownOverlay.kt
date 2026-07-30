package com.restrusher.partypuzl.ui.views.game.common

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
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink

@Composable
fun MiniGameCountdownOverlay(
    countdownValue: Int,
    modifier: Modifier = Modifier
) {
    // The frosted panel is a light wash in dark mode and a heavier white one in
    // light mode, where a 22% white veil over cream would be invisible. The wash
    // keeps its original 1 : 0.45 : 0.73 alpha falloff either way.
    val glass = MaterialTheme.appColors.glassTint
    val glassEdge = MaterialTheme.appColors.glassEdge
    val onGlass = MaterialTheme.appColors.onGlass

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    glass,
                    glass.copy(alpha = glass.alpha * 0.45f),
                    glass.copy(alpha = glass.alpha * 0.73f)
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
                            glassEdge,
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
                .background(glassEdge.copy(alpha = glassEdge.alpha * 0.35f))
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
                            color = onGlass.ink(Ink.Standard),
                            offset = Offset.Zero,
                            blurRadius = 24f
                        )
                    ),
                    fontWeight = FontWeight.Bold,
                    color = onGlass
                )
                if (value > 0) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = Shadow(
                                color = onGlass.ink(Ink.Tertiary),
                                offset = Offset.Zero,
                                blurRadius = 16f
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = onGlass
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun CountdownOverlayNumberPreview() {
    PartyPuzlTheme { MiniGameCountdownOverlay(countdownValue = 2) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun CountdownOverlayGoPreview() {
    PartyPuzlTheme { MiniGameCountdownOverlay(countdownValue = 0) }
}
