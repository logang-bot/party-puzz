package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import kotlin.math.floor

private const val FULL_SPINS = 2

private val previewLabels = listOf(
    "Take a shot of tequila",
    "Down a shot the group picks for you",
    "Confess your most-used emoji, out loud, dramatically",
    "Take 3 drink(s)!"
)

private val reelShape = RoundedCornerShape(20.dp)

/**
 * Whips away too fast to read, then decelerates hard so only the approach is legible.
 *
 * A symmetric ease-in-out spreads its speed evenly and never reads as a slot machine: every row
 * gets the same glance. Front-loading almost all of the travel holds the opening row for at most
 * 22 ms — a deliberate blur — and the closing three for 139 ms or more, which is where the player
 * actually reads the outcome. Figures hold for decks of 7 to 9 entries over a 1950 ms run.
 */
private val reelLanding = CubicBezierEasing(0.12f, 0.75f, 0.06f, 1f)

/** What the reel spins through, and where it has to stop. */
internal data class SlotReelSpec(
    val itemCount: Int,
    val targetIndex: Int,
    val durationMillis: Int
)

/**
 * Slot-machine reel that spins through the deck and decelerates onto its target.
 *
 * `itemContent` is handed `isLanded = true` for the centre row once the spin has finished, and for
 * no other row at any other moment, so a caller may attach a shared-element key to it.
 */
@Composable
internal fun SlotReel(
    reel: SlotReelSpec,
    tone: Color,
    maskColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 64.dp,
    visibleItems: Int = 3,
    itemContent: @Composable (index: Int, isLanded: Boolean) -> Unit
) {
    if (reel.itemCount <= 0) return

    val spec = ReelWindowSpec(reel.itemCount, itemHeight, visibleItems)
    val offset = remember { Animatable(0f) }
    var hasLanded by remember { mutableStateOf(false) }
    LaunchedEffect(reel) {
        hasLanded = false
        offset.snapTo(0f)
        offset.animateTo(
            spec.landingOffset(reel.targetIndex),
            tween(reel.durationMillis, easing = reelLanding)
        )
        hasLanded = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
            .clip(reelShape)
            .background(MaterialTheme.appColors.panelFill)
            .border(1.dp, tone.ink(Ink.Tertiary), reelShape)
    ) {
        ReelStrip(
            spec = spec,
            strip = ReelStripState(offset.value, hasLanded),
            itemContent = itemContent
        )
        ReelMask(maskColor = maskColor)
    }
}

@Composable
private fun ReelStrip(
    spec: ReelWindowSpec,
    strip: ReelStripState,
    itemContent: @Composable (index: Int, isLanded: Boolean) -> Unit
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { spec.itemHeight.toPx() }
    val centerPx = with(density) { spec.centerOffset.toPx() }
    val travelled = floor(strip.offset).toInt()
    val translation = centerPx - itemHeightPx * (spec.rowsAboveCenter + strip.offset - travelled)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(spec.itemHeight * spec.slotCount)
            .graphicsLayer { translationY = translation }
    ) {
        repeat(spec.slotCount) { slot ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spec.itemHeight)
                    .padding(horizontal = 16.dp)
            ) {
                itemContent(
                    spec.itemIndexAt(travelled, slot),
                    strip.hasLanded && slot == spec.rowsAboveCenter
                )
            }
        }
    }
}

@Composable
private fun ReelMask(maskColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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

private data class ReelStripState(val offset: Float, val hasLanded: Boolean)

/** Geometry of the reel window: how many rows it draws, and which item each one holds. */
private data class ReelWindowSpec(
    val itemCount: Int,
    val itemHeight: Dp,
    val visibleItems: Int
) {
    val slotCount: Int get() = visibleItems + 1

    val rowsAboveCenter: Int get() = visibleItems / 2

    val centerOffset: Dp get() = (itemHeight * visibleItems - itemHeight) / 2

    fun landingOffset(targetIndex: Int): Float = (FULL_SPINS * itemCount + targetIndex).toFloat()

    /**
     * Item drawn in [slot] once the reel has travelled [travelled] whole items.
     *
     * Slot [rowsAboveCenter] sits dead centre, so at a whole-numbered offset it holds item
     * `travelled mod itemCount`. That is what makes a landing offset of
     * `FULL_SPINS * itemCount + targetIndex` settle on `targetIndex`.
     */
    fun itemIndexAt(travelled: Int, slot: Int): Int =
        Math.floorMod(travelled - rowsAboveCenter + slot, itemCount)
}

@Preview(name = "SlotReel – Light", showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun SlotReelPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize().padding(20.dp)) { SlotReelPreviewBody() }
    }
}

@Preview(name = "SlotReel – Dark", showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun SlotReelDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize().padding(20.dp)) { SlotReelPreviewBody() }
    }
}

@Composable
private fun SlotReelPreviewBody() {
    SlotReel(
        reel = SlotReelSpec(itemCount = previewLabels.size, targetIndex = 3, durationMillis = 1950),
        tone = AccentYellow,
        maskColor = MaterialTheme.colorScheme.surface,
        itemHeight = 88.dp
    ) { index, _ ->
        Text(
            text = previewLabels[index],
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
