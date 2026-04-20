package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import kotlin.math.sqrt

private val borderColors = listOf(
    Color(0xFF00E5FF),
    Color(0xFFD500F9),
    Color(0xFFFF6D00),
    Color(0xFF00E5FF),
)

@Composable
fun MiniGamesOptionChip(
    modifier: Modifier = Modifier,
    optionName: String,
    initialEnabled: Boolean = false,
    onToggled: () -> Unit = {},
) {
    var selected by remember { mutableStateOf(initialEnabled) }
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius = 20.dp

    // One-shot: animates 0° → 360° over 3 s, then fades into a solid border
    val animatable = remember { Animatable(0f) }
    var animationDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        animationDone = true
    }

    val gradientAlpha by animateFloatAsState(
        targetValue = if (animationDone) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "gradient alpha"
    )
    val solidBorderAlpha by animateFloatAsState(
        targetValue = if (animationDone) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "solid border alpha"
    )

    // Inverted relative to other chips: they use primary bg / onPrimary text
    val enabledColor = MaterialTheme.colorScheme.onPrimary
    val enabledTextColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val textColor by animateColorAsState(
        targetValue = if (selected) enabledTextColor else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(250),
        label = "text color"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) enabledColor else surfaceVariantColor,
        animationSpec = tween(250),
        label = "bg color"
    )
    // When enabled the border matches the background, making it seamlessly invisible
    val solidBorderColor by animateColorAsState(
        targetValue = if (selected) enabledColor else primaryColor,
        animationSpec = tween(250),
        label = "border color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .drawWithContent {
                val cornerPx = cornerRadius.toPx()
                val borderPx = 1.dp.toPx()

                // Gradient border: diagonal-sized square guarantees full coverage
                // of the chip bounds at every rotation angle
                if (gradientAlpha > 0f) {
                    val diag = sqrt(size.width * size.width + size.height * size.height)
                    rotate(degrees = animatable.value, pivot = center) {
                        drawRect(
                            brush = Brush.sweepGradient(borderColors, center = center),
                            topLeft = Offset(center.x - diag / 2f, center.y - diag / 2f),
                            size = Size(diag, diag),
                            alpha = gradientAlpha
                        )
                    }
                }

                // Solid border fades in once the gradient fades out.
                // Stroke width is 2× borderPx; the outer half is clipped away,
                // leaving exactly 1 dp of visible border — matching other chips.
                if (solidBorderAlpha > 0f) {
                    drawRoundRect(
                        color = solidBorderColor,
                        cornerRadius = CornerRadius(cornerPx),
                        style = Stroke(width = borderPx * 2),
                        alpha = solidBorderAlpha
                    )
                }

                // Interior background always inset so both border styles sit in
                // the same 1 dp strip around the chip edge
                drawRoundRect(
                    color = backgroundColor,
                    cornerRadius = CornerRadius(maxOf(0f, cornerPx - borderPx)),
                    topLeft = Offset(borderPx, borderPx),
                    size = Size(size.width - borderPx * 2, size.height - borderPx * 2)
                )

                drawContent()
            }
            .clickable(interactionSource = interactionSource, indication = null) {
                selected = !selected
                onToggled()
            }
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        if (selected) {
            Image(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = stringResource(id = R.string.option_description),
                colorFilter = ColorFilter.tint(textColor),
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 2.dp)
            )
        }
        Text(text = optionName, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Preview(showBackground = true)
@Composable
fun MiniGamesOptionChipPreview() {
    PartyPuzzTheme {
        MiniGamesOptionChip(optionName = stringResource(id = R.string.mini_games))
    }
}
