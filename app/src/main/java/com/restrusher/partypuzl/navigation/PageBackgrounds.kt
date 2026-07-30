package com.restrusher.partypuzl.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.ui.common.gameModeTheme
import com.restrusher.partypuzl.ui.theme.PageBackground
import com.restrusher.partypuzl.ui.theme.TintStrength

/**
 * Which page background a route sits on.
 *
 * The design gives all but three screens their own background rather than sharing one,
 * so this is where that per-screen decision lives. It is resolved at the root scaffold
 * — not inside each screen — because the background has to paint behind the app bar
 * too; a per-screen background would leave a seam under the bar.
 *
 * [tintOverride] carries the tints a route cannot know on its own: the mode the home
 * carousel is currently showing, the party's mode, the pack's spice accent, the entry
 * type being written. Screens report those with
 * [ReportPageTint][com.restrusher.partypuzl.ui.theme.ReportPageTint]. When a screen does
 * not report one, the mode of the session being set up is used.
 */
@Composable
internal fun pageBackgroundFor(
    destination: NavDestination?,
    tintOverride: Color? = null,
): PageBackground {
    val sessionTint = gameModeTheme(GameOptionsSource.currentGameModeNameRes)
        .gradientColors
        .first()
    val tint = tintOverride ?: sessionTint

    return when {
        destination == null -> PageBackground.Warm

        // Tinted by whichever mode card the carousel is showing.
        destination.hasRoute(HomeScreen::class) -> PageBackground.Home(tintOverride)

        // The two screens the design keeps on the warm cream ramp.
        destination.hasRoute(PartiesScreen::class) ||
                destination.hasRoute(CreatePlayerScreen::class) -> PageBackground.Warm

        destination.hasRoute(SettingsScreen::class) -> PageBackground.Bright

        destination.hasRoute(PartyDetailScreen::class) -> PageBackground.TintedBright(tint)

        destination.hasRoute(GameConfigScreen::class) -> PageBackground.Tinted(tint)

        destination.hasRoute(CustomPacksRoute::class) ||
                destination.hasRoute(CreateCustomPackRoute::class) ->
            PageBackground.Tinted(tint, TintStrength.Soft)

        // Tinted by the pack being edited / the entry type being written.
        destination.hasRoute(CustomPackEditorRoute::class) ||
                destination.hasRoute(CreateCustomEntryRoute::class) ->
            PageBackground.Tinted(tint, TintStrength.Faint, baseStop = PACK_BASE_STOP)

        // The game shell and the mini-games paint their own; the shell behind them is flat.
        destination.hasRoute(GameScreen::class) ||
                destination.hasRoute(FollowTheSpotRoute::class) ||
                destination.hasRoute(HotPotatoRoute::class) ||
                destination.hasRoute(TapWarRoute::class) ||
                destination.hasRoute(SimonSaysRoute::class) ||
                destination.hasRoute(CircleMasterRoute::class) -> PageBackground.Flat

        else -> PageBackground.Tinted(tint)
    }
}

/** The pack screens reach the page base slightly higher up than the rest. */
private const val PACK_BASE_STOP = 0.46f
