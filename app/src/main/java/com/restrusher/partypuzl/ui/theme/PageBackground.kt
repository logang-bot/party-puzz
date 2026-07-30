package com.restrusher.partypuzl.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The page backgrounds the design uses, one variant per role a screen plays.
 *
 * The design does not have a single app background: `.pp-app` sets a default, and
 * then all but three screens override it with their own. The shape they override it
 * with is nearly always the same — a colored wash at the top fading into the page
 * base (`colorScheme.background`, the design's `--bg-0`) — with the wash color coming
 * from the game mode, the pack, or the deal being played.
 *
 * Variants are named for that role rather than for their geometry, so a caller passes
 * at most a tint and never has to resolve a background color itself.
 *
 * See [Modifier.appBackground][appBackground] for how each one is drawn, and
 * [pageBackgroundFor][com.restrusher.partypuzl.navigation.pageBackgroundFor] for the
 * route-by-route mapping.
 */
sealed interface PageBackground {

    /** Home — a top glow tinted by the selected game mode, over the warm cream ramp. */
    data class Home(val tint: Color? = null) : PageBackground

    /**
     * The warm cream→peach ramp, which in the design survives on only two screens:
     * the parties list and create-player. Dark has no warm ramp, so it falls back to
     * the default teal glow — which is exactly what the design's `.pp-app` is.
     */
    data object Warm : PageBackground

    /** Settings — warm at the top, fading down to [AppColors.pageBaseBright]. */
    data object Bright : PageBackground

    /** Party detail — a tinted wash fading down to [AppColors.pageBaseBright]. */
    data class TintedBright(val tint: Color? = null) : PageBackground

    /**
     * The workhorse: a wash of [tint] at the top fading into the page base by
     * [baseStop]. Game config, the pack screens, the deal picker and trivia.
     */
    data class Tinted(
        val tint: Color? = null,
        val strength: TintStrength = TintStrength.Standard,
        val baseStop: Float = 0.5f,
    ) : PageBackground

    /**
     * A top-anchored radial glow of [tint] over the page base — the challenge reveal
     * and the photo moment, where the deal's own color should bloom from the top.
     */
    data class TintedGlow(
        val tint: Color? = null,
        val strength: TintStrength = TintStrength.Standard,
        val centerYRatio: Float = 0f,
        val baseStop: Float = 0.6f,
    ) : PageBackground

    /** The flat page base. The game shell and the outcome screens, which draw their own. */
    data object Flat : PageBackground

    /** The navigation drawer sheet, which the design gives its own ramp. */
    data object Drawer : PageBackground
}

/**
 * How strongly a tint washes over the page base. The values map the alpha suffixes
 * the design's CSS uses on its gradient stops: `40`, `35`, `30`, `25`/`22`.
 */
enum class TintStrength(val alpha: Float) {
    Strong(0.25f),
    Prominent(0.21f),
    Standard(0.19f),
    Soft(0.15f),
    Faint(0.13f),
}
