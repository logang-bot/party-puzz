package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import kotlinx.coroutines.delay

private const val GRID_GAP_DP = 6
private const val GATHER_CARD_MS = 200

/** The deals on offer, in their compact state, laid out for the spotlight to travel across. */
@Composable
internal fun SpotlightGrid(
    dealTypes: List<GameDealType>,
    highlightedDealType: GameDealType?,
    isFlickering: Boolean,
    gatherMillis: Int,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(GRID_GAP_DP.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        spotlightRows(dealTypes).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(GRID_GAP_DP.dp)) {
                row.forEach { dealType ->
                    SpotlightCard(
                        dealType = dealType,
                        state = SpotlightBorderState(
                            isHighlighted = dealType == highlightedDealType,
                            isFlickering = isFlickering && dealType == highlightedDealType,
                            restingTone = dealType.accent.tone
                        ),
                        gatherDelayMillis = gatherDelayMillis(
                            gatherMillis,
                            dealTypes.indexOf(dealType),
                            dealTypes.size
                        )
                    )
                }
            }
        }
    }
}

/** Two per row once there are three or more deals, so the odd last one spans the width. */
private fun spotlightRows(dealTypes: List<GameDealType>): List<List<GameDealType>> {
    if (dealTypes.size < 3) return listOf(dealTypes)
    return listOf(dealTypes.take(2), dealTypes.drop(2))
}

@Composable
private fun RowScope.SpotlightCard(
    dealType: GameDealType,
    state: SpotlightBorderState,
    gatherDelayMillis: Int
) {
    var hasGathered by remember { mutableStateOf(false) }
    LaunchedEffect(dealType) {
        delay(gatherDelayMillis.toLong())
        hasGathered = true
    }

    AnimatedVisibility(
        visible = hasGathered,
        enter = fadeIn(tween(GATHER_CARD_MS)) + scaleIn(tween(GATHER_CARD_MS), initialScale = 0.8f),
        modifier = Modifier.weight(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .spotlightBorder(state)
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

/** Staggered so the last card has finished arriving exactly as the ring starts to travel. */
private fun gatherDelayMillis(gatherMillis: Int, index: Int, cardCount: Int): Int =
    (gatherMillis - GATHER_CARD_MS).coerceAtLeast(0) * index / (cardCount - 1).coerceAtLeast(1)

@Preview(name = "SpotlightGrid – four deals – Light", showBackground = true, widthDp = 360, heightDp = 320)
@Composable
private fun SpotlightGridPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize().padding(24.dp)) {
            SpotlightGrid(
                dealTypes = GameDealType.entries,
                highlightedDealType = GameDealType.STICKY_DARE,
                isFlickering = false,
                gatherMillis = 0
            )
        }
    }
}

@Preview(name = "SpotlightGrid – three deals – Dark", showBackground = true, widthDp = 360, heightDp = 320)
@Composable
private fun SpotlightGridDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize().padding(24.dp)) {
            SpotlightGrid(
                dealTypes = GameDealType.entries.take(3),
                highlightedDealType = GameDealType.GENERAL_KNOWLEDGE,
                isFlickering = true,
                gatherMillis = 0
            )
        }
    }
}
