package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import kotlinx.coroutines.delay

internal const val FLICKER_STEP_MS = 100

/** Inset between a compact card and the ring around it, so the two borders never overlap. */
internal val spotlightRingInset = 5.dp

private const val TONE_FADE_MS = 60
private const val RING_WIDTH_DP = 2.5f
private const val HIGHLIGHT_SCALE = 1.06f
private const val HIGHLIGHT_LIFT_MS = 140

private val spotlightRingShape = RoundedCornerShape(21.dp)

/** Which of the three states the spotlight ring is in for one card. */
internal data class SpotlightBorderState(
    val isHighlighted: Boolean,
    val isFlickering: Boolean,
    val restingTone: Color
)

/**
 * Ring the surprise spotlight travels along: invisible at rest, the card's own tone once the
 * highlight reaches it, and a cycle through every deal's tone once it has landed there.
 */
@Composable
internal fun Modifier.spotlightBorder(state: SpotlightBorderState): Modifier {
    val tone by animateColorAsState(
        spotlightTone(state), tween(TONE_FADE_MS, easing = LinearEasing), label = "spotlight tone"
    )
    val lift by animateFloatAsState(
        if (state.isHighlighted) HIGHLIGHT_SCALE else 1f, tween(HIGHLIGHT_LIFT_MS), label = "spotlight lift"
    )
    return this.scale(lift).border(RING_WIDTH_DP.dp, tone, spotlightRingShape)
}

@Composable
private fun spotlightTone(state: SpotlightBorderState): Color {
    val flickerTone = rememberFlickerTone(isFlickering = state.isFlickering)
    return when {
        state.isFlickering -> flickerTone
        state.isHighlighted -> state.restingTone
        else -> Color.Transparent
    }
}

@Composable
private fun rememberFlickerTone(isFlickering: Boolean): Color {
    var toneIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(isFlickering) {
        if (!isFlickering) return@LaunchedEffect
        while (true) {
            delay(FLICKER_STEP_MS.toLong())
            toneIndex = (toneIndex + 1) % dealTones.size
        }
    }
    return dealTones[toneIndex]
}

@Preview(name = "SpotlightBorder – Light", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun SpotlightBorderPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { SpotlightBorderPreviewBody() }
}

@Preview(name = "SpotlightBorder – Dark", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun SpotlightBorderDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { SpotlightBorderPreviewBody() }
}

@Composable
private fun SpotlightBorderPreviewBody() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.appBackground().fillMaxSize().padding(16.dp)
    ) {
        GameDealType.entries.take(3).forEachIndexed { index, dealType ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .spotlightBorder(
                        SpotlightBorderState(
                            isHighlighted = index == 1,
                            isFlickering = false,
                            restingTone = dealType.accent.tone
                        )
                    )
                    .padding(spotlightRingInset)
            ) {
                DealCompactCard(
                    accent = dealType.accent,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
