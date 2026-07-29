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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Radius of the dark background's glow, as a multiple of the screen width.
 * Mirrors the design's `radial-gradient(120% 60% at 50% 0%, …)`.
 */
private const val DARK_GLOW_RADIUS_RATIO = 1.2f

/**
 * The app-wide page background, applied once on the root scaffold so every route
 * inherits it. Light is a straight cream→peach ramp; dark is a teal glow anchored
 * at the top center, which is what gives the dark theme its depth.
 */
@Composable
fun Modifier.appBackground(): Modifier {
    val isDark = LocalDarkTheme.current
    return if (isDark) {
        drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to backgroundGradientDarkTop,
                        0.45f to backgroundGradientDarkMid,
                        1f to backgroundGradientDarkEnd,
                    ),
                    center = Offset(size.width / 2f, 0f),
                    radius = size.width * DARK_GLOW_RADIUS_RATIO,
                )
            )
        }
    } else {
        background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundGradientLightStart,
                    backgroundGradientLightMid,
                    backgroundGradientLightEnd
                )
            )
        )
    }
}

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
