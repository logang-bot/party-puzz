package com.restrusher.partypuzz.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

/**
 * A tiny clock icon with a single hand that sweeps clockwise continuously.
 * Size is controlled via [modifier]; defaults to 18×18 dp.
 *
 * The static parts (ring, tick, pivot) are drawn in one Canvas layer.
 * The hand is drawn in a separate Canvas layer rotated via [graphicsLayer],
 * which is evaluated at the draw phase without triggering recomposition —
 * giving a perfectly smooth sweep at display refresh rate.
 *
 * Because the hand is drawn pointing straight up and rotated as a graphics
 * transform, the [RepeatMode.Restart] jump from 360° back to 0° lands on the
 * exact same visual position and is imperceptible.
 */
@Composable
fun ClockHandAnimation(
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val resolvedColor = if (color == Color.Unspecified) LocalContentColor.current else color

    val infiniteTransition = rememberInfiniteTransition(label = "clock hand")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = modifier) {
        // Static layer: ring + 12-o'clock tick + center pivot
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringStroke = (radius * 0.13f).coerceAtLeast(1.5f)

            drawCircle(
                color = resolvedColor,
                radius = radius - ringStroke / 2f,
                center = center,
                style = Stroke(width = ringStroke)
            )

            val tickOuter = radius - ringStroke / 2f
            drawLine(
                color = resolvedColor,
                start = Offset(center.x, center.y - tickOuter),
                end = Offset(center.x, center.y - tickOuter + radius * 0.22f),
                strokeWidth = ringStroke,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = resolvedColor,
                radius = ringStroke * 0.9f,
                center = center
            )
        }

        // Dynamic layer: hand always points up, rotated by graphicsLayer.
        // Reading `rotation` here is a draw-phase state read — no recomposition needed.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringStroke = (radius * 0.13f).coerceAtLeast(1.5f)

            drawLine(
                color = resolvedColor,
                start = center,
                end = Offset(center.x, center.y - radius * 0.62f),
                strokeWidth = ringStroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClockHandAnimationPreview() {
    PartyPuzzTheme {
        ClockHandAnimation(
            modifier = Modifier.size(18.dp)
        )
    }
}
