package com.restrusher.partypuzz.ui.views.game.gameScreen

sealed class BarEvent {
    data object NoAction : BarEvent()
    data class GiveDrinks(val amount: Int, val targetPlayerName: String) : BarEvent()
    data class TakeDrinks(val amount: Int) : BarEvent()
}
