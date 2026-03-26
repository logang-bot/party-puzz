package com.restrusher.partypuzz.data.repositories.interfaces

import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import kotlinx.coroutines.flow.Flow

interface PartyRepository {
    suspend fun createParty(name: String): Long
    suspend fun linkPlayerToParty(partyId: Int, playerId: Int)
    fun getAllParties(): Flow<List<PartyWithPlayers>>
    suspend fun getPartyById(partyId: Int): PartyWithPlayers?
    suspend fun updateLastUsed(partyId: Int)
    suspend fun updatePartyName(partyId: Int, name: String)
    suspend fun deleteParty(partyId: Int)
}
