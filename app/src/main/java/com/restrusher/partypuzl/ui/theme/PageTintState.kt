package com.restrusher.partypuzl.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * The tint the page background is currently washed with.
 *
 * Four screens colour their background from data the route cannot see — the mode the home
 * carousel is showing, a party's last mode, a pack's spice accent, the entry type being
 * written. They report it with [ReportPageTint]; the root scaffold reads [tint] back.
 *
 * This is a holder behind a CompositionLocal rather than a `setPageTint` callback threaded
 * through each screen for two reasons: the screens stop carrying a background parameter they
 * have no other use for, and a tint can no longer outlive the screen that set it — that is
 * now the composable's own disposal rather than a route-string comparison.
 */
@Stable
class PageTintState {
    var tint by mutableStateOf<Color?>(null)
        internal set
}

/**
 * Deliberately not `staticCompositionLocalOf`: this value genuinely changes while the app is
 * running, and a static local would invalidate the whole subtree on every carousel swipe.
 */
val LocalPageTint = compositionLocalOf { PageTintState() }

/**
 * Reports [tint] as the page background's wash for as long as the caller is composed.
 *
 * Clears it on the way out, but only if nothing else has reported since — during a navigation
 * transition both screens are composed, and the arriving one must win.
 */
@Composable
fun ReportPageTint(tint: Color?) {
    val state = LocalPageTint.current
    DisposableEffect(state, tint) {
        state.tint = tint
        onDispose { if (state.tint == tint) state.tint = null }
    }
}
