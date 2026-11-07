package com.restrusher.partypuzl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** WCAG AA for body text. */
private const val MIN_CONTRAST = 4.5f
private const val DARKEN_STEP = 0.05f

/**
 * This accent as text or icon colour on the page.
 *
 * The accent palette (Color.kt) is theme-invariant and tuned for the dark page, so most
 * of it is unreadable as ink on the cream light page — lime and yellow sit under 2:1.
 * In light this darkens the accent along its own hue until it clears [MIN_CONTRAST]
 * against the page background; in dark it is returned unchanged.
 *
 * Only for ink. Fills, strokes and washes keep the raw accent.
 */
@Composable
@ReadOnlyComposable
fun Color.asPageInk(): Color =
    if (LocalDarkTheme.current) this
    else readableOn(MaterialTheme.colorScheme.background)

private fun Color.readableOn(background: Color): Color {
    val opaque = copy(alpha = 1f)
    var fraction = 0f
    var candidate = opaque
    while (contrast(candidate, background) < MIN_CONTRAST && fraction < 1f) {
        fraction += DARKEN_STEP
        candidate = lerp(opaque, Color.Black, fraction)
    }
    return candidate.copy(alpha = alpha)
}

/** WCAG contrast ratio between two opaque colours, from 1 (none) to 21. */
private fun contrast(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}
