package com.restrusher.partypuzz.ui.views.createPlayer

import com.restrusher.partypuzz.data.models.Gender

data class CreatePlayerState (
    val playerId: Int = -1,
    val playerName: String = "",
    val gender: Gender = Gender.Male
)