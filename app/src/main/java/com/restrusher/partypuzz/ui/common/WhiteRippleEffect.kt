package com.restrusher.partypuzz.ui.common

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object WhiteRippleEffect: RippleTheme {

    @Composable
    override fun defaultColor(): Color {
        return RippleTheme.defaultRippleColor(
            Color.White,
            lightTheme = false
        )
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleTheme.defaultRippleAlpha(
            Color.Black,
            lightTheme = false
        )
    }

}