package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.ui.common.gameModeTheme
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.AccentViolet
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.BrandTealDeep
import com.restrusher.partypuzl.ui.theme.BrandTealShade
import com.restrusher.partypuzl.ui.theme.PageBackground
import com.restrusher.partypuzl.ui.theme.TintStrength

/** The turn phases reach the page base higher up than a normal screen does. */
private const val TURN_BASE_STOP = 0.55f

/** The photo moment blooms from just below the top rather than from the very edge. */
private const val PHOTO_GLOW_CENTER_Y = 0.2f
private const val PHOTO_BASE_STOP = 0.7f

/**
 * Background for the game screen, which changes with the turn rather than staying put.
 *
 * Picking a deal washes the screen in the game mode's colour, so Bar, Couples and Party Puzl each
 * feel like their own room; revealing a challenge switches the wash to that deal's own accent, so
 * a truth and a dare do not look alike. Reward/punishment drops to the flat base, because the
 * medallion it spins carries all the colour the moment needs.
 *
 * The mode palette is the same one the mode cards and party cards use, from [gameModeTheme].
 */
@Composable
internal fun rememberGameBackground(
    uiState: GameScreenState,
    gameModeNameRes: Int? = GameOptionsSource.currentGameModeNameRes,
): PageBackground {
    val modeTint = gameModeTheme(gameModeNameRes).gradientColors.first()
    // Once a truth-or-dare pick is made, the reveal follows the pick's own accent rather than the
    // shared truth-or-dare pink — a truth reads teal, a dare reads pink.
    val dealTint = uiState.truthOrDareChoice?.accent?.tone ?: uiState.dealType?.accent?.tone
    return remember(uiState.dealPhase, uiState.hasActiveModeEvent, uiState.showCameraRequest, modeTint, dealTint) {
        when {
            uiState.hasActiveModeEvent -> PageBackground.Flat
            uiState.showCameraRequest -> PageBackground.TintedGlow(
                tint = modeTint,
                strength = TintStrength.Prominent,
                centerYRatio = PHOTO_GLOW_CENTER_Y,
                baseStop = PHOTO_BASE_STOP,
            )
            uiState.dealPhase == GameDealPhase.CHALLENGE_SHOWN ->
                PageBackground.TintedGlow(tint = dealTint)
            else -> PageBackground.Tinted(tint = modeTint, baseStop = TURN_BASE_STOP)
        }
    }
}

/** Visual identity of one pickable deal, shared by the deal picker and the surprise reel. */
internal data class DealAccent(
    val tone: Color,
    val gradient: List<Color>,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    @StringRes val kickerRes: Int,
    @StringRes val blurbRes: Int
)

internal val truthAccent = DealAccent(
    tone = BrandTeal,
    gradient = listOf(BrandTeal, BrandTealShade),
    iconRes = R.drawable.ic_chat_bubble,
    labelRes = R.string.truth,
    kickerRes = R.string.deal_kicker_truth,
    blurbRes = R.string.deal_blurb_truth
)

internal val dareAccent = DealAccent(
    tone = AccentPink,
    gradient = listOf(AccentPink, AccentViolet),
    iconRes = R.drawable.ic_whatshot,
    labelRes = R.string.dare,
    kickerRes = R.string.deal_kicker_dare,
    blurbRes = R.string.deal_blurb_dare
)

private val truthOrDareAccent = DealAccent(
    tone = AccentPink,
    gradient = listOf(AccentPink, AccentViolet),
    iconRes = R.drawable.ic_whatshot,
    labelRes = R.string.truth_or_dare,
    kickerRes = R.string.deal_kicker_dare,
    blurbRes = R.string.deal_blurb_dare
)

private val generalKnowledgeAccent = DealAccent(
    tone = AccentLime,
    gradient = listOf(AccentLime, BrandTealDeep),
    iconRes = R.drawable.ic_lightbulb,
    labelRes = R.string.general_knowledge_title,
    kickerRes = R.string.deal_kicker_general_knowledge,
    blurbRes = R.string.deal_blurb_general_knowledge
)

private val stickyDareAccent = DealAccent(
    tone = AccentViolet,
    gradient = listOf(AccentViolet, BrandTealShade),
    iconRes = R.drawable.ic_hourglass,
    labelRes = R.string.sticky_dares,
    kickerRes = R.string.deal_kicker_sticky_dare,
    blurbRes = R.string.deal_blurb_sticky_dare
)

private val miniGameAccent = DealAccent(
    tone = AccentYellow,
    gradient = listOf(AccentYellow, AccentCoral),
    iconRes = R.drawable.ic_random,
    labelRes = R.string.mini_games,
    kickerRes = R.string.deal_kicker_mini_game,
    blurbRes = R.string.deal_blurb_mini_game
)

internal val GameDealType.accent: DealAccent
    get() = when (this) {
        GameDealType.TRUTH_OR_DARE -> truthOrDareAccent
        GameDealType.GENERAL_KNOWLEDGE -> generalKnowledgeAccent
        GameDealType.STICKY_DARE -> stickyDareAccent
        GameDealType.MINI_GAME -> miniGameAccent
    }

internal val TruthOrDareChoice.accent: DealAccent
    get() = when (this) {
        TruthOrDareChoice.TRUTH -> truthAccent
        TruthOrDareChoice.DARE -> dareAccent
    }

internal val dealCardShape = RoundedCornerShape(22.dp)
internal val dealCompactShape = RoundedCornerShape(16.dp)
internal val playerCardShape = RoundedCornerShape(11.dp)
