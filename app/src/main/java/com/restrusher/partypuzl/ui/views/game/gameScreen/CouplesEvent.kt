package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.restrusher.partypuzl.R

sealed class CouplesEvent {
    data object GiveAKiss : CouplesEvent()
    data object ChooseKissers : CouplesEvent()
    data class MakeALoveDeclaration(val targetPlayerName: String) : CouplesEvent()
    data class ActOfLove(val requesterPlayerName: String) : CouplesEvent()
    data object ChooseLovers : CouplesEvent()

    /** A deck line that carries no mechanics — the text is the whole outcome. */
    data class FlavourReward(@StringRes val textRes: Int) : CouplesEvent()

    data class FlavourPunishment(@StringRes val textRes: Int) : CouplesEvent()
}

/** Null for the flavour lines, which have no artwork of their own and fall back to the medallion. */
@get:DrawableRes
val CouplesEvent.imageRes: Int?
    get() = when (this) {
        is CouplesEvent.GiveAKiss -> R.drawable.img_kiss
        is CouplesEvent.ChooseKissers -> R.drawable.img_choose_kissers
        is CouplesEvent.MakeALoveDeclaration -> R.drawable.img_love_declaration
        is CouplesEvent.ActOfLove -> R.drawable.img_love_act
        is CouplesEvent.ChooseLovers -> R.drawable.img_lovers
        is CouplesEvent.FlavourReward,
        is CouplesEvent.FlavourPunishment -> null
    }

val CouplesEvent.category: EventCategory
    get() = when (this) {
        is CouplesEvent.MakeALoveDeclaration,
        is CouplesEvent.ActOfLove,
        is CouplesEvent.FlavourPunishment -> EventCategory.PUNISHMENT
        is CouplesEvent.GiveAKiss,
        is CouplesEvent.ChooseKissers,
        is CouplesEvent.ChooseLovers,
        is CouplesEvent.FlavourReward -> EventCategory.REWARD
    }
