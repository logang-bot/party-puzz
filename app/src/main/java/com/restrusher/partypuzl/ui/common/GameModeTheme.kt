package com.restrusher.partypuzl.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.AccentViolet
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.BrandTealDeep
import com.restrusher.partypuzl.ui.theme.BrandTealShade
import com.restrusher.partypuzl.ui.theme.ModeFallbackEnd
import com.restrusher.partypuzl.ui.theme.ModeFallbackStart

data class GameModeTheme(
    val gradientColors: List<Color>,
    @DrawableRes val iconId: Int
)

private val StandardModeTheme = GameModeTheme(
    gradientColors = listOf(BrandTeal, BrandTealShade),
    iconId = R.drawable.ic_standard
)

private val BarModeTheme = GameModeTheme(
    gradientColors = listOf(AccentCoral, AccentPink),
    iconId = R.drawable.ic_bar
)

private val CouplesModeTheme = GameModeTheme(
    gradientColors = listOf(AccentPink, AccentViolet),
    iconId = R.drawable.ic_couples
)

private val PartyPuzlModeTheme = GameModeTheme(
    gradientColors = listOf(AccentLime, BrandTealDeep),
    iconId = R.drawable.ic_partypuzz
)

private val DefaultModeTheme = GameModeTheme(
    gradientColors = listOf(ModeFallbackStart, ModeFallbackEnd),
    iconId = R.drawable.ic_standard
)

fun gameModeTheme(gameModeNameRes: Int?): GameModeTheme = when (gameModeNameRes) {
    R.string.standard_game_mode -> StandardModeTheme
    R.string.bar_game_mode -> BarModeTheme
    R.string.couples_game_mode -> CouplesModeTheme
    R.string.party_puzz_game_mode -> PartyPuzlModeTheme
    else -> DefaultModeTheme
}
