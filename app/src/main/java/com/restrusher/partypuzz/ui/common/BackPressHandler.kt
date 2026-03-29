package com.restrusher.partypuzz.ui.common

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/**
 * Intercepts back presses and requires two consecutive taps within [resetDelayMs] milliseconds
 * to actually exit. Shows a [Toast] with [warningMessage] on the first press.
 */
@Composable
fun BackPressToExit(
    warningMessage: String,
    resetDelayMs: Long = 2000L,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }

    BackHandler {
        if (backPressedOnce) {
            onExit()
        } else {
            backPressedOnce = true
            Toast.makeText(context, warningMessage, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(resetDelayMs)
            backPressedOnce = false
        }
    }
}
