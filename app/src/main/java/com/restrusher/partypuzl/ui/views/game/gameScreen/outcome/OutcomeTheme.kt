package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.AccentRose
import com.restrusher.partypuzl.ui.theme.AccentViolet
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.OutcomePunishCrimson
import com.restrusher.partypuzl.ui.theme.OutcomePunishMidnight
import com.restrusher.partypuzl.ui.theme.OutcomePunishPlum
import com.restrusher.partypuzl.ui.theme.OutcomePunishRoseDeep
import com.restrusher.partypuzl.ui.views.game.gameScreen.EventCategory
import com.restrusher.partypuzl.ui.views.game.gameScreen.GameScreenState

internal enum class OutcomeMode { BAR, COUPLES }

/**
 * Which mode owns the event currently on screen. Couples wins ties because Party Puzl can leave
 * both sub-modes active, and the couples visual is the more specific one.
 */
internal val GameScreenState.activeOutcomeMode: OutcomeMode?
    get() = when {
        couplesMode.activeEvent != null -> OutcomeMode.COUPLES
        barMode.activeEvent != null -> OutcomeMode.BAR
        else -> null
    }

/**
 * Look of one reward / punishment reveal. Rewards run bright and warm, punishments run dark and
 * saturated, so the two read differently across the room before anybody reads the text.
 */
internal data class OutcomeTheme(
    val gradient: List<Color>,
    val tone: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val kickerRes: Int,
    @StringRes val rollingRes: Int
)

private val couplesReward = OutcomeTheme(
    gradient = listOf(AccentPink, AccentViolet),
    tone = AccentPink,
    iconRes = R.drawable.ic_couples,
    kickerRes = R.string.outcome_reward,
    rollingRes = R.string.outcome_rolling_reward
)

private val couplesPunishment = OutcomeTheme(
    gradient = listOf(OutcomePunishRoseDeep, OutcomePunishPlum),
    tone = AccentRose,
    iconRes = R.drawable.ic_couples,
    kickerRes = R.string.outcome_punishment,
    rollingRes = R.string.outcome_rolling_punishment
)

private val barReward = OutcomeTheme(
    gradient = listOf(AccentYellow, AccentPink),
    tone = AccentYellow,
    iconRes = R.drawable.ic_sports_bar,
    kickerRes = R.string.outcome_reward,
    rollingRes = R.string.outcome_rolling_reward
)

private val barPunishment = OutcomeTheme(
    gradient = listOf(OutcomePunishCrimson, OutcomePunishMidnight),
    tone = OutcomePunishCrimson,
    iconRes = R.drawable.ic_whatshot,
    kickerRes = R.string.outcome_punishment,
    rollingRes = R.string.outcome_rolling_punishment
)

internal fun outcomeTheme(mode: OutcomeMode, category: EventCategory): OutcomeTheme = when {
    mode == OutcomeMode.COUPLES && category == EventCategory.REWARD -> couplesReward
    mode == OutcomeMode.COUPLES -> couplesPunishment
    category == EventCategory.REWARD -> barReward
    else -> barPunishment
}
