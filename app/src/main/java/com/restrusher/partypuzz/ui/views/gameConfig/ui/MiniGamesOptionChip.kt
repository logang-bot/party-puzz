package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
fun MiniGamesOptionCard(
    optionName: String,
    modifier: Modifier = Modifier,
    initialEnabled: Boolean = false,
    onToggled: () -> Unit = {},
) {
    var selected by remember { mutableStateOf(initialEnabled) }
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius = 12.dp

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

    val checkBgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(250),
        label = "check bg"
    )
    val solidBorderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(250),
        label = "border color"
    )
    val bgColor = MaterialTheme.colorScheme.surfaceContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawWithContent {
                val cornerPx = cornerRadius.toPx()
                val borderPx = 1.dp.toPx()

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

                if (solidBorderAlpha > 0f) {
                    drawRoundRect(
                        color = solidBorderColor,
                        cornerRadius = CornerRadius(cornerPx),
                        style = Stroke(width = borderPx * 2),
                        alpha = solidBorderAlpha
                    )
                }

                drawRoundRect(
                    color = bgColor,
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
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        OptionCardContent(
            optionName = optionName,
            selected = selected,
            checkBgColor = checkBgColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MiniGamesOptionCardPreview() {
    PartyPuzzTheme {
        MiniGamesOptionCard(optionName = stringResource(id = R.string.mini_games))
    }
}
