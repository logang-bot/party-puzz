package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.ui.common.gameModeTheme
import com.restrusher.partypuzl.ui.theme.LocalDarkTheme
import com.restrusher.partypuzl.ui.theme.backgroundGradientDarkEnd
import com.restrusher.partypuzl.ui.theme.backgroundGradientDarkStart
import com.restrusher.partypuzl.ui.theme.backgroundGradientLightEnd
import com.restrusher.partypuzl.ui.theme.backgroundGradientLightMid
import com.restrusher.partypuzl.ui.theme.backgroundGradientLightStart

private const val DARK_MODE_TINT_ALPHA = 0.38f
private const val LIGHT_MODE_TINT_ALPHA = 0.22f

/**
 * Background for the whole game screen, tinted by the game mode being played so Bar, Couples and
 * Party Puzl each feel like their own room. The mode palette is the same one the mode cards and
 * party cards use, from [gameModeTheme].
 */
@Composable
internal fun rememberBackgroundGradient(gameModeNameRes: Int? = GameOptionsSource.currentGameModeNameRes): Brush {
    val isDark = LocalDarkTheme.current
    val tint = gameModeTheme(gameModeNameRes).gradientColors.first()
    return remember(isDark, tint) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    tint.copy(alpha = DARK_MODE_TINT_ALPHA).compositeOver(backgroundGradientDarkEnd),
                    backgroundGradientDarkEnd,
                    backgroundGradientDarkStart
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    tint.copy(alpha = LIGHT_MODE_TINT_ALPHA).compositeOver(backgroundGradientLightStart),
                    backgroundGradientLightMid,
                    backgroundGradientLightEnd
                )
            )
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
    tone = Color(0xFF2EB6C6),
    gradient = listOf(Color(0xFF2EB6C6), Color(0xFF1C4F5C)),
    iconRes = R.drawable.ic_chat_bubble,
    labelRes = R.string.truth,
    kickerRes = R.string.deal_kicker_truth,
    blurbRes = R.string.deal_blurb_truth
)

internal val dareAccent = DealAccent(
    tone = Color(0xFFFF5B8A),
    gradient = listOf(Color(0xFFFF5B8A), Color(0xFF8B6CFF)),
    iconRes = R.drawable.ic_whatshot,
    labelRes = R.string.dare,
    kickerRes = R.string.deal_kicker_dare,
    blurbRes = R.string.deal_blurb_dare
)

private val truthOrDareAccent = DealAccent(
    tone = Color(0xFFFF5B8A),
    gradient = listOf(Color(0xFFFF5B8A), Color(0xFF8B6CFF)),
    iconRes = R.drawable.ic_whatshot,
    labelRes = R.string.truth_or_dare,
    kickerRes = R.string.deal_kicker_dare,
    blurbRes = R.string.deal_blurb_dare
)

private val generalKnowledgeAccent = DealAccent(
    tone = Color(0xFFA8E063),
    gradient = listOf(Color(0xFFA8E063), Color(0xFF1C7A87)),
    iconRes = R.drawable.ic_lightbulb,
    labelRes = R.string.general_knowledge_title,
    kickerRes = R.string.deal_kicker_general_knowledge,
    blurbRes = R.string.deal_blurb_general_knowledge
)

private val stickyDareAccent = DealAccent(
    tone = Color(0xFF8B6CFF),
    gradient = listOf(Color(0xFF8B6CFF), Color(0xFF1C4F5C)),
    iconRes = R.drawable.ic_hourglass,
    labelRes = R.string.sticky_dares,
    kickerRes = R.string.deal_kicker_sticky_dare,
    blurbRes = R.string.deal_blurb_sticky_dare
)

private val miniGameAccent = DealAccent(
    tone = Color(0xFFFFD25A),
    gradient = listOf(Color(0xFFFFD25A), Color(0xFFFF8A5C)),
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
