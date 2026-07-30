package com.restrusher.partypuzl.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// Named alpha, for the same reason Color.kt has named hex.
//
// These are the only alphas allowed on a scheme colour outside this package. The
// codebase previously carried 36 distinct hand-typed values across 53 files —
// five different ones for "secondary caption text" alone — which meant no call
// site could be compared to any other. Naming the rungs makes the ramp a
// decision made once rather than per screen.
//
// Both scales keep the alpha rather than resolving to an opaque colour: this ink
// sits on the gradients `Modifier.appBackground` paints, so it has to blend
// against whatever pixel is actually behind it.
// ─────────────────────────────────────────────────────────────

/**
 * How strongly ink reads against the page.
 *
 * Rungs are 10% apart. Anything closer is not a distinction the eye makes on these
 * backgrounds, and the gap is what stops the scale drifting back into 36 values.
 */
enum class Ink(val alpha: Float) {
    Strong(0.85f),
    Prominent(0.75f),
    Standard(0.65f),
    Secondary(0.55f),
    Tertiary(0.45f),
    Muted(0.35f),
    Faint(0.25f),
}

/**
 * Alpha for a colour used as a *fill* or a hairline rather than as ink — a tinted
 * row, a chip, a divider. Much lower than [Ink]: past ~0.18 a wash stops reading as
 * a surface and starts reading as text.
 */
enum class Wash(val alpha: Float) {
    Hairline(0.15f),
    Fill(0.12f),
    Soft(0.08f),
    Faint(0.05f),
}

/** This colour as ink at [level]. */
fun Color.ink(level: Ink): Color = copy(alpha = level.alpha)

/** This colour as a fill or hairline at [level]. */
fun Color.wash(level: Wash): Color = copy(alpha = level.alpha)
