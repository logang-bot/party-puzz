package com.restrusher.partypuzl.ui.views.game.miniGames.circleMaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.ink

@Composable
internal fun CircleCanvas(
    drawnPath: List<Offset>,
    isDrawingEnabled: Boolean,
    onDrawPoint: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.error

    val path = remember(drawnPath) {
        Path().apply {
            drawnPath.forEachIndexed { i, pt ->
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }
    }

    Canvas(
        modifier = modifier.pointerInput(isDrawingEnabled) {
            if (!isDrawingEnabled) return@pointerInput
            detectDragGestures(
                onDragStart = { offset -> onDrawPoint(offset) },
                onDrag = { change, _ ->
                    change.consume()
                    onDrawPoint(change.position)
                },
                onDragEnd = onDrawEnd
            )
        }
    ) {
        // Outer glow ring around center dot
        drawCircle(
            color = dotColor.ink(Ink.Faint),
            radius = 14.dp.toPx(),
            center = center
        )
        // Center target dot
        drawCircle(
            color = dotColor,
            radius = 8.dp.toPx(),
            center = center
        )
        // Drawn path
        if (drawnPath.size > 1) {
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
