package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarModeState
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesModeState
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.game.gameScreen.GameScreenState
import com.restrusher.partypuzl.ui.views.game.gameScreen.OUTCOME_SPIN_DURATION_MS

private const val REEL_HOLD_MS = 250

/** Three wrapped lines of [MaterialTheme.typography] titleMedium, plus room inside the border. */
private val outcomeRowHeight = 88.dp

private val previewBarDeck = barPunishmentDeck()
private val previewCouplesDeck = couplesRewardDeck()

private val previewBarState = GameScreenState(
    barMode = BarModeState(isActive = true, activeEvent = previewBarDeck[2], deck = previewBarDeck)
)

private val previewCouplesState = GameScreenState(
    couplesMode = CouplesModeState(
        isActive = true,
        activeEvent = previewCouplesDeck[1],
        deck = previewCouplesDeck
    )
)

/**
 * The roll that precedes a reward or punishment landing.
 *
 * The reel cycles this game's actual deck and stops on the outcome that really fired, so the row
 * it settles on is the line the reveal goes on to show.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.OutcomeSpinContent(
    uiState: GameScreenState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val mode = uiState.activeOutcomeMode ?: return
    val category = uiState.activeEventCategory ?: return
    val theme = outcomeTheme(mode, category)
    val texts = outcomeDeckTexts(uiState, mode)
    if (texts.isEmpty()) return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(theme.gradient))
        ) {
            Icon(
                painter = painterResource(theme.iconRes),
                contentDescription = null,
                tint = MaterialTheme.appColors.onAccentSurface,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(theme.rollingRes).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.sp,
            color = theme.tone,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        SlotReel(
            reel = SlotReelSpec(
                itemCount = texts.size,
                targetIndex = outcomeTargetIndex(uiState, mode).coerceIn(0, texts.size - 1),
                durationMillis = (OUTCOME_SPIN_DURATION_MS - REEL_HOLD_MS).toInt()
            ),
            tone = theme.tone,
            maskColor = MaterialTheme.colorScheme.surface,
            itemHeight = outcomeRowHeight,
            modifier = Modifier.widthIn(max = 320.dp)
        ) { index, isLanded ->
            OutcomeReelRow(
                text = texts[index],
                isLanded = isLanded,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
}

@Composable
private fun outcomeDeckTexts(uiState: GameScreenState, mode: OutcomeMode): List<String> =
    if (mode == OutcomeMode.COUPLES) {
        uiState.couplesMode.deck.map { couplesMessage(it) }
    } else {
        uiState.barMode.deck.map { barMessage(it) }
    }

private fun outcomeTargetIndex(uiState: GameScreenState, mode: OutcomeMode): Int =
    if (mode == OutcomeMode.COUPLES) {
        uiState.couplesMode.deck.indexOf(uiState.couplesMode.activeEvent)
    } else {
        uiState.barMode.deck.indexOf(uiState.barMode.activeEvent)
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.OutcomeReelRow(
    text: String,
    isLanded: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = if (isLanded) {
            Modifier.outcomeTextBounds(this@OutcomeReelRow, animatedVisibilityScope)
        } else {
            Modifier
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "OutcomeSpin – bar punishment – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinContentPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { OutcomeSpinPreviewBody(previewBarState) }
}

@Preview(name = "OutcomeSpin – bar punishment – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinContentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { OutcomeSpinPreviewBody(previewBarState) }
}

@Preview(name = "OutcomeSpin – couples reward – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinCouplesRewardPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { OutcomeSpinPreviewBody(previewCouplesState) }
}

@Preview(name = "OutcomeSpin – couples reward – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinCouplesRewardDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { OutcomeSpinPreviewBody(previewCouplesState) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun OutcomeSpinPreviewBody(uiState: GameScreenState) {
    Box(Modifier.appBackground().fillMaxSize()) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                OutcomeSpinContent(
                    uiState = uiState,
                    animatedVisibilityScope = this@AnimatedVisibility
                )
            }
        }
    }
}
