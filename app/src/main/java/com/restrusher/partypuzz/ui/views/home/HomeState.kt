package com.restrusher.partypuzz.ui.views.home

import com.restrusher.partypuzz.data.local.appData.appModels.GameMode
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers

data class HomeState(
    val allParties: List<PartyWithPlayers> = emptyList(),
    val gameModes: List<GameMode> = emptyList(),
    val isLoading: Boolean = true,
    val isPartySelected: Boolean = false,
    val isDialogOpen: Boolean = false,
    val dialogPendingPartyId: Int? = null
) {
    val activeParty: PartyWithPlayers? get() = allParties.firstOrNull()
    val hasParties: Boolean get() = !isLoading && activeParty != null
    val activePlayers: List<PlayerEntity> get() = if (isPartySelected) activeParty?.players ?: emptyList() else emptyList()
}
