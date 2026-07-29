package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val FULL_SPINS = 4
private val reelShape = RoundedCornerShape(20.dp)

/**
 * Slot-machine reel that spins through [itemCount] entries and decelerates onto [targetIndex].
 * Shared by the "Surprise me" deal shuffle and the reward / punishment roll.
 */
@Composable
internal fun SlotReel(
    itemCount: Int,
    targetIndex: Int,
    durationMillis: Int,
    tone: Color,
    maskColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 64.dp,
    visibleItems: Int = 3,
    itemContent: @Composable (index: Int) -> Unit
) {
    if (itemCount <= 0) return

    val density = LocalDensity.current
    val windowHeight = itemHeight * visibleItems
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val centerPx = with(density) { ((windowHeight - itemHeight) / 2).toPx() }
    val landingOffset = (FULL_SPINS * itemCount + targetIndex).toFloat()
    val reelLength = FULL_SPINS * itemCount + targetIndex + visibleItems

    val offset = remember { Animatable(0f) }
    LaunchedEffect(itemCount, targetIndex) {
        offset.snapTo(0f)
        offset.animateTo(landingOffset, tween(durationMillis, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(windowHeight)
            .clip(reelShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .border(1.dp, tone.copy(alpha = 0.4f), reelShape)
    ) {
        // requiredHeight, not height: the parent Box is only `windowHeight` tall, and a plain
        // Column would hand every row past the first `visibleItems` a maxHeight of 0 — they would
        // still be laid out, but with no height, so the reel would look empty while spinning.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(itemHeight * reelLength)
                .offset { IntOffset(0, (centerPx - offset.value * itemHeightPx).roundToInt()) }
        ) {
            repeat(reelLength) { position ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 16.dp)
                ) {
                    itemContent(position % itemCount)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(itemHeight - 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, tone, RoundedCornerShape(14.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to maskColor,
                        0.3f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to maskColor
                    )
                )
        )
    }
}
