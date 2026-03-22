package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import kotlinx.coroutines.delay

private val SPOT_DIAMETER = 56.dp
private val BORDER_WIDTH = 3.dp
private val DIVIDER_GRADIENT_HEIGHT = 48.dp
private const val BORDER_COLOR_STEP_MS = 600

private val BorderColors = listOf(
    Color(0xFFFF80AB), // light pink
    Color(0xFF80D8FF), // light blue
    Color(0xFFCCFF90), // light green
    Color(0xFFFFFF8D), // light yellow
)

internal enum class DividerEdge { Top, Bottom }

@Composable
internal fun SpotBoard(
    spotNormX: Float,
    spotNormY: Float,
    isActive: Boolean,
    dividerEdge: DividerEdge,
    onSpotTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val spotDiameterPx = remember(density) { with(density) { SPOT_DIAMETER.roundToPx() } }

    // Cycles through BorderColors while the game is running
    var colorIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                delay(BORDER_COLOR_STEP_MS.toLong())
                colorIndex = (colorIndex + 1) % BorderColors.size
            }
        }
    }
    val loopingBorderColor by animateColorAsState(
        targetValue = BorderColors[colorIndex],
        animationSpec = tween(durationMillis = BORDER_COLOR_STEP_MS, easing = LinearEasing),
        label = "borderColor"
    )

    // Fades borders in when active, out when inactive
    val borderAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "borderAlpha"
    )

    // Restores the surface background when the game ends (original inactive design)
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) Color.Transparent else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor"
    )

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
            .background(backgroundColor)
            .onSizeChanged { boardSize = it }
            .drawBehind {
                val strokePx = BORDER_WIDTH.toPx()
                val gradHeightPx = DIVIDER_GRADIENT_HEIGHT.toPx()
                val borderColor = loopingBorderColor.copy(alpha = borderAlpha)

                // Divider-touching edge: gradient fading from the border color (at divider seam)
                // to transparent (toward the SpotBoard interior)
                when (dividerEdge) {
                    DividerEdge.Bottom -> drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, borderColor),
                            startY = size.height - gradHeightPx,
                            endY = size.height
                        ),
                        topLeft = Offset(0f, size.height - gradHeightPx),
                        size = Size(size.width, gradHeightPx)
                    )
                    DividerEdge.Top -> drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(borderColor, Color.Transparent),
                            startY = 0f,
                            endY = gradHeightPx
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, gradHeightPx)
                    )
                }

                // Device-edge solid borders (left and right are always present)
                drawRect(
                    color = borderColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(strokePx, size.height)
                )
                drawRect(
                    color = borderColor,
                    topLeft = Offset(size.width - strokePx, 0f),
                    size = Size(strokePx, size.height)
                )
                // Top or bottom device edge (whichever is NOT touching the divider)
                when (dividerEdge) {
                    DividerEdge.Bottom -> drawRect(
                        color = borderColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, strokePx)
                    )
                    DividerEdge.Top -> drawRect(
                        color = borderColor,
                        topLeft = Offset(0f, size.height - strokePx),
                        size = Size(size.width, strokePx)
                    )
                }
            }
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

@Preview(name = "SpotBoard – active (divider at bottom)", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardActivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.4f,
            spotNormY = 0.5f,
            isActive = true,
            dividerEdge = DividerEdge.Bottom,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "SpotBoard – inactive (divider at top)", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardInactivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.7f,
            spotNormY = 0.3f,
            isActive = false,
            dividerEdge = DividerEdge.Top,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
