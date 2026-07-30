package com.restrusher.partypuzl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

// How each PageBackground variant is actually drawn. The variants themselves, and the
// reasoning behind having more than one, live in PageBackground.kt.

/**
 * Radius of a radial glow, as a multiple of the screen width. Encodes the `120%` in
 * the design's `radial-gradient(120% 60% at 50% 0%, …)`.
 */
private const val GLOW_RADIUS_RATIO = 1.2f

/**
 * Paints [background] behind the content.
 *
 * Applied once on the root scaffold in `HomeNavigation`, so it sits *behind* the app
 * bar as well as the route content — moving it onto the individual screens would
 * leave a seam under the bar. The game screen and the previews are the exceptions:
 * they call this directly.
 */
@Composable
fun Modifier.appBackground(
    background: PageBackground = PageBackground.Tinted(),
): Modifier {
    val palette = backgroundPalette()
    return when (background) {
        is PageBackground.Home -> glow(
            tint = background.tint,
            // Dark carries the mode color harder — it has more room before it washes out.
            strength = if (palette.isDark) TintStrength.Strong else TintStrength.Standard,
            stops = palette.glowStops,
            baseStop = HOME_BASE_STOP,
        )

        PageBackground.Warm -> if (palette.isDark) {
            glow(tint = null, strength = TintStrength.Standard, stops = palette.glowStops)
        } else {
            verticalRamp(palette.warmRamp)
        }

        // Light lifts to white; dark has nowhere brighter to go, so it keeps the glow.
        PageBackground.Bright -> if (palette.isDark) {
            glow(tint = null, strength = TintStrength.Standard, stops = palette.glowStops)
        } else {
            verticalRamp(
                listOf(palette.warmTop, palette.bright, palette.bright),
                midStop = BRIGHT_BASE_STOP,
            )
        }

        is PageBackground.TintedBright -> if (palette.isDark) {
            verticalRamp(
                listOf(
                    palette.wash(background.tint, TintStrength.Strong),
                    palette.base,
                    palette.base,
                ),
                midStop = TINTED_BRIGHT_DARK_BASE_STOP,
            )
        } else {
            // Passes through the warm cream on the way to white, which is what keeps the
            // scrapbook's photo cards from floating on a flat sheet.
            verticalRamp(
                listOf(
                    palette.wash(background.tint, TintStrength.Standard),
                    palette.warmTop,
                    palette.bright,
                ),
            )
        }

        is PageBackground.Tinted -> verticalRamp(
            listOf(
                palette.wash(background.tint, background.strength),
                palette.base,
                palette.base,
            ),
            midStop = background.baseStop,
        )

        is PageBackground.TintedGlow -> glow(
            tint = background.tint,
            strength = background.strength,
            stops = listOf(palette.base, palette.base),
            centerYRatio = background.centerYRatio,
            baseStop = background.baseStop,
        )

        PageBackground.Flat -> drawBehind { drawRect(color = palette.base) }

        PageBackground.Drawer -> verticalRamp(palette.drawerRamp, midStop = null)
    }
}

/** Settings fades to white by 70% of the page height in the design. */
private const val BRIGHT_BASE_STOP = 0.7f

/** Home's glow reaches its mid stop slightly lower than the default `.pp-app` glow does. */
private const val HOME_BASE_STOP = 0.5f

/** Party detail holds its mode wash further down in dark, where there is more room for it. */
private const val TINTED_BRIGHT_DARK_BASE_STOP = 0.6f

/**
 * A vertical gradient of [colors]. When [midStop] is set the second color lands
 * there and holds to the bottom, which is how the design writes
 * `linear-gradient(180deg, <tint> 0%, var(--bg-0) 50%)` — the base is reached
 * partway down rather than at the very bottom.
 */
private fun Modifier.verticalRamp(colors: List<Color>, midStop: Float? = 0.5f): Modifier =
    drawBehind {
        val brush = if (midStop == null || colors.size < 3) {
            Brush.verticalGradient(colors = colors, startY = 0f, endY = size.height)
        } else {
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to colors[0],
                    midStop to colors[1],
                    1f to colors[2],
                ),
                startY = 0f,
                endY = size.height,
            )
        }
        drawRect(brush = brush)
    }

/**
 * A radial glow anchored at the horizontal center, [centerYRatio] down the screen.
 * [stops] are the colors the glow falls off into, reached at [baseStop] and at the edge.
 */
private fun Modifier.glow(
    tint: Color?,
    strength: TintStrength,
    stops: List<Color>,
    centerYRatio: Float = 0f,
    baseStop: Float = 0.45f,
): Modifier = drawBehind {
    val top = stops.first()
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to (tint?.copy(alpha = strength.alpha)?.compositeOver(top) ?: top),
                baseStop to stops[stops.lastIndex - 1],
                1f to stops.last(),
            ),
            center = Offset(size.width / 2f, size.height * centerYRatio),
            radius = size.width * GLOW_RADIUS_RATIO,
        )
    )
}

/**
 * The colors every variant is built from, resolved once per composition.
 *
 * Both themes are represented in one shape so the variants above read as a single
 * `when` instead of branching on the theme in eight places.
 */
private class BackgroundPalette(
    val isDark: Boolean,
    /** The page base — the design's `--bg-0`. */
    val base: Color,
    /** The brighter end party detail and settings fade to. */
    val bright: Color,
    /** Top stop of the warm cream ramp. */
    val warmTop: Color,
    /** The full warm cream→peach ramp. */
    val warmRamp: List<Color>,
    /** Top / mid / end of the glow's fall-off. */
    val glowStops: List<Color>,
    val drawerRamp: List<Color>,
) {
    /** [tint] washed at [strength] over the page base, or the bare base when untinted. */
    fun wash(tint: Color?, strength: TintStrength): Color =
        tint?.copy(alpha = strength.alpha)?.compositeOver(base) ?: base
}

@Composable
@ReadOnlyComposable
private fun backgroundPalette(): BackgroundPalette {
    val isDark = LocalDarkTheme.current
    val base = MaterialTheme.colorScheme.background
    return BackgroundPalette(
        isDark = isDark,
        base = base,
        bright = MaterialTheme.appColors.pageBaseBright,
        warmTop = if (isDark) backgroundGradientDarkTop else backgroundGradientLightStart,
        warmRamp = if (isDark) {
            listOf(backgroundGradientDarkTop, backgroundGradientDarkMid, backgroundGradientDarkEnd)
        } else {
            listOf(backgroundGradientLightStart, backgroundGradientLightMid, backgroundGradientLightEnd)
        },
        glowStops = if (isDark) {
            listOf(backgroundGradientDarkTop, backgroundGradientDarkMid, backgroundGradientDarkEnd)
        } else {
            listOf(backgroundGradientLightStart, backgroundGradientLightStart, backgroundGradientLightMid)
        },
        drawerRamp = if (isDark) {
            listOf(drawerGradientDarkTop, drawerGradientDarkBottom)
        } else {
            listOf(drawerGradientLightTop, drawerGradientLightBottom)
        },
    )
}
