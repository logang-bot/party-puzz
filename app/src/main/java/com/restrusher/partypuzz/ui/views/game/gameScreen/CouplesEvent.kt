package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.annotation.DrawableRes
import com.restrusher.partypuzz.R

sealed class CouplesEvent {
    data object GiveAKiss : CouplesEvent()
    data object ChooseKissers : CouplesEvent()
    data class MakeALoveDeclaration(val targetPlayerName: String) : CouplesEvent()
    data class ActOfLove(val requesterPlayerName: String) : CouplesEvent()
    data object ChooseLovers : CouplesEvent()
}

@get:DrawableRes
val CouplesEvent.imageRes: Int
    get() = when (this) {
        is CouplesEvent.GiveAKiss -> R.drawable.img_kiss
        is CouplesEvent.ChooseKissers -> R.drawable.img_choose_kissers
        is CouplesEvent.MakeALoveDeclaration -> R.drawable.img_love_declaration
        is CouplesEvent.ActOfLove -> R.drawable.img_love_act
        is CouplesEvent.ChooseLovers -> R.drawable.img_lovers
    }

val CouplesEvent.category: EventCategory
    get() = when (this) {
        is CouplesEvent.MakeALoveDeclaration,
        is CouplesEvent.ActOfLove -> EventCategory.PUNISHMENT
        is CouplesEvent.GiveAKiss,
        is CouplesEvent.ChooseKissers,
        is CouplesEvent.ChooseLovers -> EventCategory.REWARD
    }
