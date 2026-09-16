package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarEvent
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarModeState
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesEvent
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesModeState
import com.restrusher.partypuzl.ui.views.game.gameScreen.DealOptionButton
import com.restrusher.partypuzl.ui.views.game.gameScreen.GameScreenState
import com.restrusher.partypuzl.ui.views.game.gameScreen.imageRes

/** The landed reward or punishment, popped in after [OutcomeSpinContent] finishes rolling. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.OutcomeRevealContent(
    uiState: GameScreenState,
    onGiveDrinksTargetSelected: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val mode = uiState.activeOutcomeMode ?: return
    val category = uiState.activeEventCategory ?: return
    val theme = outcomeTheme(mode, category)
    val couplesEvent = uiState.couplesMode.activeEvent
    val barEvent = uiState.barMode.activeEvent

    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        pop.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        val artwork = couplesEvent?.imageRes.takeIf { mode == OutcomeMode.COUPLES }
        Box(modifier = Modifier.scale(pop.value)) {
            if (artwork != null) {
                Image(
                    painter = painterResource(artwork),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(theme.gradient))
                ) {
                    Icon(
                        painter = painterResource(theme.iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.appColors.onAccentSurface,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(theme.kickerRes).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.sp,
            color = theme.tone,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .outcomeTextBounds(this@OutcomeRevealContent, animatedVisibilityScope)
        ) {
            Text(
                text = outcomeMessage(barEvent, couplesEvent),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))

        if (barEvent is BarEvent.GiveDrinksPickTarget) {
            barEvent.candidates.forEach { name ->
                DealOptionButton(
                    text = name,
                    onClick = { onGiveDrinksTargetSelected(name) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        } else {
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary),
                textAlign = TextAlign.Center
            )
        }
    }
}

private val previewBarState = GameScreenState(
    barMode = BarModeState(isActive = true, activeEvent = BarEvent.TakeDrinks(amount = 3))
)

private val previewCouplesState = GameScreenState(
    couplesMode = CouplesModeState(isActive = true, activeEvent = CouplesEvent.GiveAKiss)
)

@Preview(name = "OutcomeReveal – bar punishment – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealContentPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { OutcomeRevealPreviewBody(previewBarState) }
}

@Preview(name = "OutcomeReveal – bar punishment – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealContentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { OutcomeRevealPreviewBody(previewBarState) }
}

@Preview(name = "OutcomeReveal – couples reward – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealCouplesRewardPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { OutcomeRevealPreviewBody(previewCouplesState) }
}

@Preview(name = "OutcomeReveal – couples reward – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealCouplesRewardDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { OutcomeRevealPreviewBody(previewCouplesState) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun OutcomeRevealPreviewBody(uiState: GameScreenState) {
    Box(Modifier.appBackground().fillMaxSize()) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                OutcomeRevealContent(
                    uiState = uiState,
                    onGiveDrinksTargetSelected = {},
                    animatedVisibilityScope = this@AnimatedVisibility
                )
            }
        }
    }
}
