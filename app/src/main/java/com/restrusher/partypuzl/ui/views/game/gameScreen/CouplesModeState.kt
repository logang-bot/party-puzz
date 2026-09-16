package com.restrusher.partypuzl.ui.views.game.gameScreen

data class CouplesModeState(
    val isActive: Boolean = false,
    val activeEvent: CouplesEvent? = null,
    val deck: List<CouplesEvent> = emptyList()
)
