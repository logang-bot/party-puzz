package com.restrusher.partypuzl.ui.views.parties

import com.restrusher.partypuzl.data.local.entities.PartyWithPlayers

data class PartiesState(
    val parties: List<PartyWithPlayers> = emptyList(),
    val isLoading: Boolean = true,
    val totalPhotoCount: Int = 0
)
