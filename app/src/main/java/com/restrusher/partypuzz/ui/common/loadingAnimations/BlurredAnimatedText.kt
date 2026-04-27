package com.restrusher.partypuzz.ui.common.loadingAnimations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val ANIMATION_DURATION = 1000

@Composable
fun BlurredAnimatedText(
    text: String,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val resolvedColor = if (color == Color.Unspecified) LocalContentColor.current else color
    val blurList = text.mapIndexed { index, character ->
        if(character == ' ') {
            remember {
                mutableFloatStateOf(0f)
            }
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "infinite transition $index")
            infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        offsetMillis = (ANIMATION_DURATION / text.length) * index
                    )
                ),
                label = "blur animation"
            )
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.forEachIndexed { index, character ->
            val blurAmount = blurList[index].value
            Text(
                text = character.toString(),
                color = resolvedColor,
                modifier = if (character != ' ' && blurAmount > 0f) {
                    Modifier.blur(blurAmount.dp, BlurredEdgeTreatment.Unbounded)
                } else {
                    Modifier
                }
            )
        }
    }
}
