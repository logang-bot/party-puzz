package com.restrusher.partypuzl.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A raised card surface.
 *
 * The two themes lift a card off the page in different ways, and swapping one for
 * the other does not work: dark uses a translucent white wash plus a hairline
 * border, which is invisible against cream, so light uses an opaque white fill
 * plus a soft drop shadow instead.
 *
 * Note the shadow draws outside the composable's bounds — an ancestor that clips
 * will cut it off.
 */
@Composable
fun Modifier.appCard(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 8.dp,
): Modifier {
    val colors = MaterialTheme.appColors
    val isDark = LocalDarkTheme.current
    val lifted = if (isDark) {
        this
    } else {
        shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = colors.cardShadow,
            spotColor = colors.cardShadow,
        )
    }
    return lifted
        .clip(shape)
        .background(color = colors.cardSurface, shape = shape)
        .border(width = 1.dp, color = colors.cardBorder, shape = shape)
}

/** The design's `linear-gradient(180deg, transparent, var(--bg-0) 50%)` — reached halfway down. */
private const val CTA_SCRIM_BASE_STOP = 0.5f

/**
 * The fade behind a bottom-pinned call to action, so page content dissolves into the page base
 * under the button instead of stopping at a hard edge.
 *
 * Apply it on the *caller's* modifier, before `navigationBarsPadding()`: the scrim then paints
 * the whole strip including the area behind the system bar, and stays outside whatever alpha the
 * button applies to itself when disabled.
 *
 * [baseColor] must be the colour the page's own background settles on at the bottom —
 * `colorScheme.background` for the [PageBackground.Tinted] screens, [AppColors.pageBaseWarm] or
 * [AppColors.pageBaseBright] for the ramps that end somewhere else.
 */
@Composable
fun Modifier.ctaScrim(
    baseColor: Color = MaterialTheme.colorScheme.background,
): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                // Fading to a zero-alpha *base*, not Color.Transparent: the latter is transparent
                // black, which would drag a grey cast through the cream light theme.
                0f to baseColor.copy(alpha = 0f),
                CTA_SCRIM_BASE_STOP to baseColor,
                1f to baseColor,
            ),
            startY = 0f,
            endY = size.height,
        )
    )
}
