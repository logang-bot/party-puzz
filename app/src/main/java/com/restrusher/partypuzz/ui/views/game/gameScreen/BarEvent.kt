package com.restrusher.partypuzz.ui.views.game.gameScreen

sealed class BarEvent {
    data object NoAction : BarEvent()
    data class GiveDrinks(val amount: Int, val targetPlayerName: String) : BarEvent()
    data class GiveDrinksPickTarget(val amount: Int, val candidates: List<String>) : BarEvent()
    data class TakeDrinks(val amount: Int) : BarEvent()
}

val BarEvent.category: EventCategory
    get() = when (this) {
        is BarEvent.TakeDrinks -> EventCategory.PUNISHMENT
        is BarEvent.NoAction,
        is BarEvent.GiveDrinks,
        is BarEvent.GiveDrinksPickTarget -> EventCategory.REWARD
    }
