package com.restrusher.partypuzz.ui.views.game.gameScreen

sealed class CouplesEvent {
    data object GiveAKiss : CouplesEvent()
    data object ChoseKissers : CouplesEvent()
    data class MakeALoveDeclaration(val targetPlayerName: String) : CouplesEvent()
    data class ActOfLove(val requesterPlayerName: String) : CouplesEvent()
    data object ChoseLovers : CouplesEvent()
}
