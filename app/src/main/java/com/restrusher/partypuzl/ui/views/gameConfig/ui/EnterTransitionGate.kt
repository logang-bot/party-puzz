package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

/**
 * True once the screen's enter transition has finished, and stays true from then on.
 *
 * Content that lands while the shared-element transition from Home is running competes with it
 * for frames and resizes the page mid-flight, which is what made the transition stutter. Sections
 * that load data hold their content back until this flips.
 *
 * Saveable so it survives the screen leaving composition while it sits on the back stack:
 * coming back from CreatePlayer must render the player cards at once, or their shared bounds
 * have nothing to land on.
 */
@Composable
internal fun rememberEnterTransitionSettled(scope: AnimatedVisibilityScope): Boolean =
    awaitSettled(rememberSaveable { mutableStateOf(false) }, scope)

/**
 * Like [rememberEnterTransitionSettled], but false again every time the screen re-enters
 * composition — including the pop back from CreatePlayer.
 *
 * For heavy content nothing shared lands on (the pack rows): composing it all in the first frame
 * of the return transition is what made that transition stutter, so it waits on every visit.
 */
@Composable
internal fun rememberTransitionSettledThisVisit(scope: AnimatedVisibilityScope): Boolean =
    awaitSettled(remember { mutableStateOf(false) }, scope)

@Composable
private fun awaitSettled(settled: MutableState<Boolean>, scope: AnimatedVisibilityScope): Boolean {
    if (!settled.value) {
        LaunchedEffect(scope) {
            snapshotFlow { scope.transition.hasSettledVisible() }.first { it }
            settled.value = true
        }
    }
    return settled.value
}

private fun Transition<EnterExitState>.hasSettledVisible() =
    currentState == EnterExitState.Visible && targetState == EnterExitState.Visible
