package com.restrusher.partypuzz.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun Modifier.appBackground(): Modifier {
    val isDark = LocalDarkTheme.current
    val colors = if (isDark) {
        listOf(backgroundGradientDarkStart, backgroundGradientDarkEnd)
    } else {
        listOf(backgroundGradientLightStart, backgroundGradientLightMid, backgroundGradientLightEnd)
    }
    return background(brush = Brush.verticalGradient(colors = colors))
}
