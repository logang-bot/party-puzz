package com.restrusher.partypuzl.ui.views.customPacks.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Matches the design's `.pp-btn:disabled { opacity: 0.45 }`. */
private const val DISABLED_ALPHA = 0.45f

/**
 * The sticky bottom call to action every screen in this flow ends with. Same recipe as
 * `StartGameButton` on the setup screen — a `Box` rather than a `Button` so the pressed state can
 * swap fill and label colours — but with a caller-supplied icon and label.
 */
@Composable
internal fun CustomPackCta(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) onPrimary else primary,
        animationSpec = tween(durationMillis = 300),
        label = "cta background"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) primary else onPrimary,
        animationSpec = tween(durationMillis = 300),
        label = "cta label"
    )

    // Disabled keeps the solid brand fill and dims the whole button, so it never reads as a
    // see-through bar over the background gradient.
    val bgColor = if (enabled) animatedBgColor else primary
    val textColor = if (enabled) animatedTextColor else onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(color = bgColor, shape = RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label.uppercase(), fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
