package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarEvent
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesEvent

/**
 * The one place an outcome turns into words.
 *
 * The reel and the reveal both read through here, which is what makes the row the reel lands on
 * and the line the reveal shows the same string by construction rather than by agreement.
 */
@Composable
internal fun outcomeMessage(barEvent: BarEvent?, couplesEvent: CouplesEvent?): String = when {
    couplesEvent != null -> couplesMessage(couplesEvent)
    barEvent != null -> barMessage(barEvent)
    else -> ""
}

@Composable
internal fun couplesMessage(event: CouplesEvent): String = when (event) {
    is CouplesEvent.GiveAKiss -> stringResource(R.string.couples_event_give_a_kiss)
    is CouplesEvent.ChooseKissers -> stringResource(R.string.couples_event_chose_kissers)
    is CouplesEvent.MakeALoveDeclaration ->
        stringResource(R.string.couples_event_make_love_declaration, event.targetPlayerName)
    is CouplesEvent.ActOfLove ->
        stringResource(R.string.couples_event_act_of_love, event.requesterPlayerName)
    is CouplesEvent.ChooseLovers -> stringResource(R.string.couples_event_chose_lovers)
    is CouplesEvent.FlavourReward -> stringResource(event.textRes)
    is CouplesEvent.FlavourPunishment -> stringResource(event.textRes)
}

@Composable
internal fun barMessage(event: BarEvent): String = when (event) {
    is BarEvent.NoAction -> stringResource(R.string.bar_event_no_action)
    is BarEvent.GiveDrinks ->
        stringResource(R.string.bar_event_give_drinks, event.amount, event.targetPlayerName)
    is BarEvent.GiveDrinksPickTarget ->
        stringResource(R.string.bar_event_give_drinks_choose, event.amount)
    is BarEvent.TakeDrinks -> stringResource(R.string.bar_event_take_drinks, event.amount)
    is BarEvent.FlavourReward -> stringResource(event.textRes)
    is BarEvent.FlavourPunishment -> stringResource(event.textRes)
}
