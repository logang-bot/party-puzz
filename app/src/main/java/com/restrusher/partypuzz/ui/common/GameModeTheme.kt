package com.restrusher.partypuzz.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzz.R

data class GameModeTheme(
    val gradientColors: List<Color>,
    @DrawableRes val iconId: Int
)

private val StandardModeTheme = GameModeTheme(
    gradientColors = listOf(Color(0xFF2EB6C6), Color(0xFF1C4F5C)),
    iconId = R.drawable.ic_standard
)

private val BarModeTheme = GameModeTheme(
    gradientColors = listOf(Color(0xFFFF8A5C), Color(0xFFFF5B8A)),
    iconId = R.drawable.ic_bar
)

private val CouplesModeTheme = GameModeTheme(
    gradientColors = listOf(Color(0xFFFF5B8A), Color(0xFF8B6CFF)),
    iconId = R.drawable.ic_couples
)

private val PartyPuzzModeTheme = GameModeTheme(
    gradientColors = listOf(Color(0xFFA8E063), Color(0xFF1C7A87)),
    iconId = R.drawable.ic_partypuzz
)

private val DefaultModeTheme = GameModeTheme(
    gradientColors = listOf(Color(0xFF2A4060), Color(0xFF162840)),
    iconId = R.drawable.ic_standard
)

fun gameModeTheme(gameModeNameRes: Int?): GameModeTheme = when (gameModeNameRes) {
    R.string.standard_game_mode -> StandardModeTheme
    R.string.bar_game_mode -> BarModeTheme
    R.string.couples_game_mode -> CouplesModeTheme
    R.string.party_puzz_game_mode -> PartyPuzzModeTheme
    else -> DefaultModeTheme
}
