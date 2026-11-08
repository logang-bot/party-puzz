package com.restrusher.partypuzl.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Picks which responsible-drinking dialog, if any, sits over the app.
 *
 * - [noticeAccepted] `false`: the 18+ [AgeConfirmationDialog], until the player confirms.
 * - [noticeAccepted] `true`: the [DrinkingReminderDialog], once per launch.
 * - `null` (preferences still loading): nothing.
 *
 * "Once per launch" is held in saved state, so returning from the background or rotating doesn't
 * show it again, but a fresh start (Back out, swiped from Recents, process restart) does.
 * Accepting the age dialog counts as this launch's reminder.
 */
@Composable
fun ResponsibleDrinkingPrompts(
    noticeAccepted: Boolean?,
    onAccept: () -> Unit,
    onExit: () -> Unit
) {
    var reminderShown by rememberSaveable { mutableStateOf(false) }
    when (noticeAccepted) {
        false -> AgeConfirmationDialog(
            onAccept = {
                reminderShown = true
                onAccept()
            },
            onExit = onExit
        )
        true -> if (!reminderShown) DrinkingReminderDialog(onDismiss = { reminderShown = true })
        null -> Unit
    }
}
