package com.restrusher.partypuzz.ui.views.game

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val backgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1B1B2F), Color(0xFF162447), Color(0xFF1F4068))
)

internal val glassBrush = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
)

internal val glassCardShape = RoundedCornerShape(24.dp)
internal val playerCardShape = RoundedCornerShape(12.dp)
