package com.restrusher.partypuzl.ui.views.game.miniGames.simonSays

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.SimonBlue
import com.restrusher.partypuzl.ui.theme.SimonGreen
import com.restrusher.partypuzl.ui.theme.SimonRed
import com.restrusher.partypuzl.ui.theme.SimonYellow
import com.restrusher.partypuzl.ui.theme.ink
import kotlin.math.hypot
import kotlinx.coroutines.launch

internal val SimonColors = listOf(SimonGreen, SimonRed, SimonBlue, SimonYellow)

// Outward scatter direction per card index (matches 2×2 grid layout)
private val ScatterDirs = listOf(
    Offset(-1f, -1f),  // 0 top-left
    Offset(1f, -1f),   // 1 top-right
    Offset(-1f, 1f),   // 2 bottom-left
    Offset(1f, 1f),    // 3 bottom-right
)

private const val RIPPLE_DURATION_MS = 400
private const val SCATTER_DURATION_MS = 350

@Composable
internal fun SimonButton(
    colorIndex: Int,
    isHighlighted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    isGameOver: Boolean,
    modifier: Modifier = Modifier
) {
    // ── Ripple ────────────────────────────────────────────────────────────────
    var tapCount by remember { mutableIntStateOf(0) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var maxRippleRadius by remember { mutableFloatStateOf(0f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    LaunchedEffect(tapCount) {
        if (tapCount == 0) return@LaunchedEffect
        rippleRadius.snapTo(0f)
        rippleAlpha.snapTo(0.5f)
        launch { rippleRadius.animateTo(maxRippleRadius, tween(RIPPLE_DURATION_MS, easing = LinearEasing)) }
        rippleAlpha.animateTo(0f, tween(RIPPLE_DURATION_MS))
    }
    val currentRippleRadius = rippleRadius.value
    val currentRippleAlpha = rippleAlpha.value

    // ── Scatter ───────────────────────────────────────────────────────────────
    val scatterDist = with(LocalDensity.current) { 300.dp.toPx() }
    val dir = ScatterDirs[colorIndex]
    val scatterX by animateFloatAsState(
        targetValue = if (isGameOver) dir.x * scatterDist else 0f,
        animationSpec = tween(SCATTER_DURATION_MS),
        label = "scatterX_$colorIndex"
    )
    val scatterY by animateFloatAsState(
        targetValue = if (isGameOver) dir.y * scatterDist else 0f,
        animationSpec = tween(SCATTER_DURATION_MS),
        label = "scatterY_$colorIndex"
    )
    val scatterAlpha by animateFloatAsState(
        targetValue = if (isGameOver) 0f else 1f,
        animationSpec = tween(SCATTER_DURATION_MS - 50),
        label = "scatterAlpha_$colorIndex"
    )

    // ── Color ─────────────────────────────────────────────────────────────────
    val baseColor = SimonColors[colorIndex]
    val animatedColor by animateColorAsState(
        targetValue = if (isHighlighted) baseColor else baseColor.ink(Ink.Faint),
        animationSpec = tween(120),
        label = "simonButtonColor"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = scatterX
                translationY = scatterY
                alpha = scatterAlpha
            }
            .clip(RoundedCornerShape(20.dp))
            .background(animatedColor)
            .pointerInput(isEnabled, isGameOver) {
                if (!isEnabled || isGameOver) return@pointerInput
                maxRippleRadius = hypot(size.width.toFloat(), size.height.toFloat())
                detectTapGestures { offset ->
                    tapOffset = offset
                    tapCount++
                    onClick()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = baseColor.copy(alpha = currentRippleAlpha),
                radius = currentRippleRadius,
                center = tapOffset
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "SimonButton – highlighted (Light)", showBackground = true)
@Preview(name = "SimonButton – highlighted (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonButtonHighlightedPreview() {
    PartyPuzlTheme {
        SimonButton(colorIndex = 0, isHighlighted = true, isEnabled = false, onClick = {}, isGameOver = false, modifier = Modifier.size(120.dp))
    }
}

@Preview(name = "SimonButton – dim (Light)", showBackground = true)
@Preview(name = "SimonButton – dim (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimonButtonDimPreview() {
    PartyPuzlTheme {
        SimonButton(colorIndex = 1, isHighlighted = false, isEnabled = true, onClick = {}, isGameOver = false, modifier = Modifier.size(120.dp))
    }
}
