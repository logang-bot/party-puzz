package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
internal fun rememberBackgroundGradient(): Brush {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF1B1B2F), Color(0xFF162447), Color(0xFF1F4068))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFD6EAF5), Color(0xFFBDD8EE), Color(0xFF9FC5E2))
            )
        }
    }
}

internal val glassBrush = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
)

internal val glassCardShape = RoundedCornerShape(24.dp)
internal val playerCardShape = RoundedCornerShape(12.dp)
