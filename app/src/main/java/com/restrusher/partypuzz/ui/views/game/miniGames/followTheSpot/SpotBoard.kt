package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

private val SPOT_DIAMETER = 56.dp

@Composable
internal fun SpotBoard(
    spotNormX: Float,
    spotNormY: Float,
    isActive: Boolean,
    onSpotTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val spotDiameterPx = remember(density) { with(density) { SPOT_DIAMETER.roundToPx() } }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .onSizeChanged { boardSize = it }
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = ((boardSize.width - spotDiameterPx) * spotNormX).toInt(),
                        y = ((boardSize.height - spotDiameterPx) * spotNormY).toInt()
                    )
                }
                .size(SPOT_DIAMETER)
                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                .then(
                    if (isActive) Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSpotTapped
                    ) else Modifier
                )
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "SpotBoard – active", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardActivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.4f,
            spotNormY = 0.5f,
            isActive = true,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "SpotBoard – inactive", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun SpotBoardInactivePreview() {
    PartyPuzzTheme {
        SpotBoard(
            spotNormX = 0.7f,
            spotNormY = 0.3f,
            isActive = false,
            onSpotTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
