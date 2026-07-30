package com.restrusher.partypuzl.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
