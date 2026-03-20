package com.restrusher.partypuzz.ui.views.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun StickyDarePill(
    activeDares: List<ActiveStickyDare>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestDare = activeDares.lastOrNull { !it.isCompleted }
    val interactionSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = latestDare != null,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400)),
        modifier = modifier
    ) {
        latestDare?.let { dare ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .clickable(interactionSource = interactionSource, indication = null) { onClick() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${dare.playerName} is ${dare.presentContinuousText} for ${dare.durationLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
