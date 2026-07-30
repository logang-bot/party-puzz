package com.restrusher.partypuzl.ui.views.game.miniGames.tapWar

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.theme.MiniGameAccents
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BORDER_COLOR_STEP_MS = 600
private const val RIPPLE_RADIUS_DP = 200f
private const val RIPPLE_DURATION_MS = 500

internal enum class TapWarDividerEdge { Top, Bottom }

@Composable
internal fun TapWarSide(
    player: Player?,
    isActive: Boolean,
    dividerEdge: TapWarDividerEdge,
    isFlipped: Boolean,
    onTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    LaunchedEffect(tapCount) {
        if (tapCount == 0) return@LaunchedEffect
        rippleRadius.snapTo(0f)
        rippleAlpha.snapTo(0.4f)
        launch { rippleRadius.animateTo(RIPPLE_RADIUS_DP, tween(RIPPLE_DURATION_MS, easing = LinearEasing)) }
        rippleAlpha.animateTo(0f, tween(RIPPLE_DURATION_MS))
    }
    val currentRippleRadius = rippleRadius.value
    val currentRippleAlpha = rippleAlpha.value

    var colorIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(isActive) {
        if (isActive) while (true) {
            delay(BORDER_COLOR_STEP_MS.toLong())
            colorIndex = (colorIndex + 1) % MiniGameAccents.size
        }
    }
    val borderColor by animateColorAsState(
        targetValue = MiniGameAccents[colorIndex],
        animationSpec = tween(BORDER_COLOR_STEP_MS, easing = LinearEasing),
        label = "borderColor"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(500),
        label = "borderAlpha"
    )
    val rippleColor = MaterialTheme.appColors.onAccentSurface
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) Color.Transparent else MaterialTheme.colorScheme.surface,
        animationSpec = tween(500),
        label = "backgroundColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                val strokePx = 3.dp.toPx()
                val gradHeightPx = 48.dp.toPx()
                val color = borderColor.copy(alpha = borderAlpha)
                when (dividerEdge) {
                    TapWarDividerEdge.Bottom -> drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, color),
                            startY = size.height - gradHeightPx, endY = size.height
                        ),
                        topLeft = Offset(0f, size.height - gradHeightPx),
                        size = Size(size.width, gradHeightPx)
                    )
                    TapWarDividerEdge.Top -> drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color, Color.Transparent),
                            startY = 0f, endY = gradHeightPx
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, gradHeightPx)
                    )
                }
                drawRect(color = color, topLeft = Offset(0f, 0f), size = Size(strokePx, size.height))
                drawRect(color = color, topLeft = Offset(size.width - strokePx, 0f), size = Size(strokePx, size.height))
                when (dividerEdge) {
                    TapWarDividerEdge.Bottom -> drawRect(color = color, topLeft = Offset(0f, 0f), size = Size(size.width, strokePx))
                    TapWarDividerEdge.Top -> drawRect(color = color, topLeft = Offset(0f, size.height - strokePx), size = Size(size.width, strokePx))
                }
            }
            .pointerInput(isActive) {
                if (!isActive) return@pointerInput
                detectTapGestures { offset ->
                    tapOffset = offset
                    tapCount++
                    onTapped()
                }
            }
    ) {
        if (player != null) {
            PlayerContent(player = player, isFlipped = isFlipped)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = rippleColor.copy(alpha = currentRippleAlpha),
                radius = currentRippleRadius.dp.toPx(),
                center = tapOffset
            )
        }
    }
}

@Composable
private fun PlayerContent(player: Player, isFlipped: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (isFlipped) Modifier.rotate(180f) else Modifier
    ) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape)) {
            PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = player.nickName.substringBefore(" "),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer = Player(id = 1, nickName = "Alice", gender = Gender.Female, interestedIn = InterestedIn.Man)

@Preview(name = "TapWarSide – active (Light)", showBackground = true, widthDp = 360, heightDp = 300)
@Preview(name = "TapWarSide – active (Dark)", showBackground = true, widthDp = 360, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TapWarSideActivePreview() {
    PartyPuzlTheme {
        TapWarSide(
            player = previewPlayer,
            isActive = true,
            dividerEdge = TapWarDividerEdge.Top,
            isFlipped = false,
            onTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "TapWarSide – inactive (Light)", showBackground = true, widthDp = 360, heightDp = 300)
@Preview(name = "TapWarSide – inactive (Dark)", showBackground = true, widthDp = 360, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TapWarSideInactivePreview() {
    PartyPuzlTheme {
        TapWarSide(
            player = previewPlayer,
            isActive = false,
            dividerEdge = TapWarDividerEdge.Bottom,
            isFlipped = true,
            onTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
