package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
internal fun ModeEventChallengeContent(
    uiState: GameScreenState,
    onDismiss: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barEvent = uiState.barMode.activeEvent
    val couplesEvent = uiState.couplesMode.activeEvent

    if (barEvent == null && couplesEvent == null) return

    val titleRes = if (couplesEvent != null) R.string.couples_event_title else R.string.bar_event_title

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        when {
            barEvent is BarEvent.TakeDrinks -> DrinksFillIndicator(amount = barEvent.amount)
            barEvent != null -> Icon(
                painter = painterResource(R.drawable.ic_sports_bar),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            couplesEvent != null -> Image(
                painter = painterResource(couplesEvent.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        if (barEvent != null) {
            BarEventContent(
                event = barEvent,
                onDismiss = onDismiss,
                onGiveDrinksTargetSelected = onGiveDrinksTargetSelected
            )
        } else if (couplesEvent != null) {
            CouplesEventContent(event = couplesEvent)
        }
    }
}

@Composable
private fun BarEventContent(
    event: BarEvent,
    onDismiss: () -> Unit,
    onGiveDrinksTargetSelected: (String) -> Unit
) {
    when (event) {
        is BarEvent.NoAction -> {
            Text(
                text = stringResource(R.string.bar_event_no_action),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
        is BarEvent.GiveDrinks -> {
            Text(
                text = stringResource(R.string.bar_event_give_drinks, event.amount, event.targetPlayerName),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
        is BarEvent.GiveDrinksPickTarget -> {
            Text(
                text = stringResource(R.string.bar_event_give_drinks_choose, event.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            event.candidates.forEach { name ->
                DealOptionButton(
                    text = name,
                    onClick = { onGiveDrinksTargetSelected(name) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        is BarEvent.TakeDrinks -> {
            Text(
                text = stringResource(R.string.bar_event_take_drinks, event.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tap_to_dismiss),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CouplesEventContent(event: CouplesEvent) {
    val message = when (event) {
        is CouplesEvent.GiveAKiss -> stringResource(R.string.couples_event_give_a_kiss)
        is CouplesEvent.ChooseKissers -> stringResource(R.string.couples_event_chose_kissers)
        is CouplesEvent.MakeALoveDeclaration -> stringResource(R.string.couples_event_make_love_declaration, event.targetPlayerName)
        is CouplesEvent.ActOfLove -> stringResource(R.string.couples_event_act_of_love, event.requesterPlayerName)
        is CouplesEvent.ChooseLovers -> stringResource(R.string.couples_event_chose_lovers)
    }
    Text(
        text = message,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.tap_to_dismiss),
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.45f),
        textAlign = TextAlign.Center
    )
}

@Preview(name = "ModeEvent – bar TakeDrinks – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun ModeEventTakeDrinksLightPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.LIGHT) {
        Column(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            ModeEventChallengeContent(
                uiState = GameScreenState(
                    barMode = BarModeState(isActive = true, activeEvent = BarEvent.TakeDrinks(amount = 3))
                ),
                onDismiss = {},
                onGiveDrinksTargetSelected = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "ModeEvent – couples kiss – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun ModeEventCouplesKissDarkPreview() {
    PartyPuzzTheme(themeMode = ThemeMode.DARK) {
        Column(Modifier.background(Color(0xFF162447)).fillMaxSize()) {
            ModeEventChallengeContent(
                uiState = GameScreenState(
                    couplesMode = CouplesModeState(isActive = true, activeEvent = CouplesEvent.GiveAKiss)
                ),
                onDismiss = {},
                onGiveDrinksTargetSelected = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
