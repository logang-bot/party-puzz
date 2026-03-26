package com.restrusher.partypuzz.ui.views.parties

import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers

data class PartiesState(
    val parties: List<PartyWithPlayers> = emptyList(),
    val isLoading: Boolean = true
)
