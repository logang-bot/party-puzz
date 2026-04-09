package com.restrusher.partypuzz.ui.views.game.gameScreen

sealed class CouplesEvent {
    data object GiveAKiss : CouplesEvent()
    data object ChooseKissers : CouplesEvent()
    data class MakeALoveDeclaration(val targetPlayerName: String) : CouplesEvent()
    data class ActOfLove(val requesterPlayerName: String) : CouplesEvent()
    data object ChooseLovers : CouplesEvent()
}

val CouplesEvent.category: EventCategory
    get() = when (this) {
        is CouplesEvent.MakeALoveDeclaration,
        is CouplesEvent.ActOfLove -> EventCategory.PUNISHMENT
        is CouplesEvent.GiveAKiss,
        is CouplesEvent.ChooseKissers,
        is CouplesEvent.ChooseLovers -> EventCategory.REWARD
    }
