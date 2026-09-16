package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.annotation.StringRes

sealed class BarEvent {
    data object NoAction : BarEvent()
    data class GiveDrinks(val amount: Int, val targetPlayerName: String) : BarEvent()
    data class GiveDrinksPickTarget(val amount: Int, val candidates: List<String>) : BarEvent()
    data class TakeDrinks(val amount: Int) : BarEvent()

    /** A deck line that carries no mechanics — the text is the whole outcome. */
    data class FlavourReward(@StringRes val textRes: Int) : BarEvent()

    data class FlavourPunishment(@StringRes val textRes: Int) : BarEvent()
}

val BarEvent.category: EventCategory
    get() = when (this) {
        is BarEvent.TakeDrinks,
        is BarEvent.FlavourPunishment -> EventCategory.PUNISHMENT
        is BarEvent.NoAction,
        is BarEvent.GiveDrinks,
        is BarEvent.GiveDrinksPickTarget,
        is BarEvent.FlavourReward -> EventCategory.REWARD
    }
