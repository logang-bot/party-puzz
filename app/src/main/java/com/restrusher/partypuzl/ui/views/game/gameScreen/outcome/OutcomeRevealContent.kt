package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

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
@Composable
internal fun OutcomeRevealContent(
    uiState: GameScreenState,
    onGiveDrinksTargetSelected: (String) -> Unit,
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
        Box(modifier = Modifier.scale(pop.value)) {
            if (mode == OutcomeMode.COUPLES && couplesEvent != null) {
                Image(
                    painter = painterResource(couplesEvent.imageRes),
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
        Text(
            text = outcomeMessage(barEvent, couplesEvent),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
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

@Composable
private fun outcomeMessage(barEvent: BarEvent?, couplesEvent: CouplesEvent?): String = when {
    couplesEvent != null -> couplesMessage(couplesEvent)
    barEvent != null -> barMessage(barEvent)
    else -> ""
}

@Composable
private fun barMessage(event: BarEvent): String = when (event) {
    is BarEvent.NoAction -> stringResource(R.string.bar_event_no_action)
    is BarEvent.GiveDrinks ->
        stringResource(R.string.bar_event_give_drinks, event.amount, event.targetPlayerName)
    is BarEvent.GiveDrinksPickTarget ->
        stringResource(R.string.bar_event_give_drinks_choose, event.amount)
    is BarEvent.TakeDrinks -> stringResource(R.string.bar_event_take_drinks, event.amount)
}

@Composable
private fun couplesMessage(event: CouplesEvent): String = when (event) {
    is CouplesEvent.GiveAKiss -> stringResource(R.string.couples_event_give_a_kiss)
    is CouplesEvent.ChooseKissers -> stringResource(R.string.couples_event_chose_kissers)
    is CouplesEvent.MakeALoveDeclaration ->
        stringResource(R.string.couples_event_make_love_declaration, event.targetPlayerName)
    is CouplesEvent.ActOfLove ->
        stringResource(R.string.couples_event_act_of_love, event.requesterPlayerName)
    is CouplesEvent.ChooseLovers -> stringResource(R.string.couples_event_chose_lovers)
}

@Preview(name = "OutcomeReveal – bar punishment – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealBarPunishmentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize()) {
            OutcomeRevealContent(
                uiState = GameScreenState(
                    barMode = BarModeState(isActive = true, activeEvent = BarEvent.TakeDrinks(amount = 3))
                ),
                onGiveDrinksTargetSelected = {}
            )
        }
    }
}

@Preview(name = "OutcomeReveal – couples reward – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeRevealCouplesRewardLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize()) {
            OutcomeRevealContent(
                uiState = GameScreenState(
                    couplesMode = CouplesModeState(isActive = true, activeEvent = CouplesEvent.GiveAKiss)
                ),
                onGiveDrinksTargetSelected = {}
            )
        }
    }
}
