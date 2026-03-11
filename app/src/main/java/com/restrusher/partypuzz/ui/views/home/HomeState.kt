package com.restrusher.partypuzz.ui.views.home

import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers

data class HomeState(
    val lastParty: PartyWithPlayers? = null,
    val isLoading: Boolean = true
)
