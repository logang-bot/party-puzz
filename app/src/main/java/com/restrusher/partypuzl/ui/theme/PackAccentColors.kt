package com.restrusher.partypuzl.ui.theme

import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.data.models.PackAccent

/**
 * What each [PackAccent] actually looks like.
 *
 * The catalog names an accent, this resolves it — the same split `gameModeTheme` uses for game
 * modes. Theme invariant, like the rest of the accent ramp: pack chips are colour fills with
 * white ink on top in both themes.
 */
val PackAccent.color: Color
    get() = when (this) {
        PackAccent.TEAL -> BrandTeal
        PackAccent.PINK -> AccentPink
        PackAccent.CORAL -> AccentCoral
        PackAccent.VIOLET -> AccentViolet
        PackAccent.SKY -> AccentSky
        PackAccent.ROSE -> AccentRose
        PackAccent.LIME -> AccentLime
        PackAccent.YELLOW -> AccentYellow
    }
