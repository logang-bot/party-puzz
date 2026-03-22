package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

private val SPOT_DIAMETER = 56.dp

@Composable
internal fun SpotBoard(
    spotNormX: Float,
    spotNormY: Float,
    isActive: Boolean,
    onSpotTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val spotDiameterPx = remember(density) { with(density) { SPOT_DIAMETER.roundToPx() } }

    val gradientTopColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "gradientTop"
    )
    val gradientBottomColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "gradientBottom"
    )
    val brush = Brush.verticalGradient(listOf(gradientTopColor, gradientBottomColor))

    val spotScale = remember { Animatable(0f) }
    var displayNormX by remember { mutableFloatStateOf(spotNormX) }
    var displayNormY by remember { mutableFloatStateOf(spotNormY) }

    LaunchedEffect(spotNormX, spotNormY) {
        if (spotScale.value > 0f) {
            spotScale.animateTo(0f, animationSpec = tween(durationMillis = 150))
        }
        displayNormX = spotNormX
        displayNormY = spotNormY
        spotScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = modifier
            .background(brush)
            .onSizeChanged { boardSize = it }
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = ((boardSize.width - spotDiameterPx) * displayNormX).toInt(),
                        y = ((boardSize.height - spotDiameterPx) * displayNormY).toInt()
                    )
                }
                .size(SPOT_DIAMETER)
                .scale(spotScale.value)
                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                .then(
                    if (isActive) Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSpotTapped
                    ) else Modifier
                )
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "SpotBoard – active", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardActivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.4f,
            spotNormY = 0.5f,
            isActive = true,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "SpotBoard – inactive", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardInactivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.7f,
            spotNormY = 0.3f,
            isActive = false,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
