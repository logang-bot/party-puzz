package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarEvent
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesEvent
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
    gradient = listOf(Color(0xFFFF5B8A), Color(0xFF8B6CFF)),
    tone = Color(0xFFFF5B8A),
    iconRes = R.drawable.ic_couples,
    kickerRes = R.string.outcome_reward,
    rollingRes = R.string.outcome_rolling_reward
)

private val couplesPunishment = OutcomeTheme(
    gradient = listOf(Color(0xFF7A2140), Color(0xFF3A1030)),
    tone = Color(0xFFC23368),
    iconRes = R.drawable.ic_couples,
    kickerRes = R.string.outcome_punishment,
    rollingRes = R.string.outcome_rolling_punishment
)

private val barReward = OutcomeTheme(
    gradient = listOf(Color(0xFFFFD25A), Color(0xFFFF5B8A)),
    tone = Color(0xFFFFD25A),
    iconRes = R.drawable.ic_sports_bar,
    kickerRes = R.string.outcome_reward,
    rollingRes = R.string.outcome_rolling_reward
)

private val barPunishment = OutcomeTheme(
    gradient = listOf(Color(0xFFFF2E63), Color(0xFF1A0B2E)),
    tone = Color(0xFFFF2E63),
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

/** Labels the roll cycles through — every outcome the mode can produce, in a fixed order. */
@ArrayRes
internal fun outcomeReelLabelsRes(mode: OutcomeMode): Int = when (mode) {
    OutcomeMode.BAR -> R.array.outcome_reel_bar
    OutcomeMode.COUPLES -> R.array.outcome_reel_couples
}

/** Position of [event] inside `R.array.outcome_reel_bar`, so the reel lands on what really happened. */
internal val BarEvent.reelIndex: Int
    get() = when (this) {
        is BarEvent.NoAction -> 0
        is BarEvent.GiveDrinks -> 1
        is BarEvent.GiveDrinksPickTarget -> 2
        is BarEvent.TakeDrinks -> 3
    }

/** Position of [event] inside `R.array.outcome_reel_couples`. */
internal val CouplesEvent.reelIndex: Int
    get() = when (this) {
        is CouplesEvent.GiveAKiss -> 0
        is CouplesEvent.ChooseKissers -> 1
        is CouplesEvent.MakeALoveDeclaration -> 2
        is CouplesEvent.ActOfLove -> 3
        is CouplesEvent.ChooseLovers -> 4
    }
