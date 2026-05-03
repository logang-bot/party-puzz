package com.restrusher.partypuzz.ui.views.game.gameScreen

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

private val BeerIconSize = 280.dp
private val ArrowSize = 46.dp
private val FillColor = Color(0xFFFFA726)
private const val MaxDrinks = 5

@Composable
fun DrinksFillIndicator(
    amount: Int,
    modifier: Modifier = Modifier
) {
    val fillFraction = remember { Animatable(0f) }

    LaunchedEffect(amount) {
        fillFraction.snapTo(0f)
        fillFraction.animateTo(
            targetValue = (amount / MaxDrinks.toFloat()).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        DrinkLevelArrow(fillFraction = fillFraction.value)
        BeerFillIcon(fillFraction = fillFraction.value)
    }
}

@Composable
private fun DrinkLevelArrow(fillFraction: Float) {
    Box(
        modifier = Modifier
            .width(ArrowSize - 10.dp)
            .height(BeerIconSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back_2),
            contentDescription = null,
            tint = FillColor,
            modifier = Modifier
                .size(ArrowSize)
                .offset(y = (BeerIconSize - ArrowSize) * (1f - fillFraction))
                .graphicsLayer { rotationZ = 180f }
        )
    }
}

@Composable
private fun BeerFillIcon(fillFraction: Float) {
    Box(modifier = Modifier.size(BeerIconSize)) {
        MaskedFill(fillFraction = fillFraction)
        Icon(
            painter = painterResource(R.drawable.ic_sports_bar),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(BeerIconSize)
        )
    }
}

@Composable
private fun MaskedFill(fillFraction: Float) {
    val dstInPaint = remember { Paint().apply { blendMode = BlendMode.DstIn } }

    Box(
        modifier = Modifier
            .size(BeerIconSize)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(BeerIconSize)
                .height(BeerIconSize * fillFraction)
                .background(FillColor)
        )
        Icon(
            painter = painterResource(R.drawable.ic_sports_bar_filled),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .size(BeerIconSize)
                .drawWithContent {
                    drawIntoCanvas { canvas ->
                        canvas.saveLayer(
                            Rect(0f, 0f, size.width, size.height),
                            dstInPaint
                        )
                        drawContent()
                        canvas.restore()
                    }
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DrinksFillIndicatorPreview() {
    PartyPuzzTheme {
        DrinksFillIndicator(amount = 3)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrinksFillIndicatorDarkPreview() {
    PartyPuzzTheme {
        DrinksFillIndicator(amount = 3)
    }
}
