package com.restrusher.partypuzl.data.proxies

import com.restrusher.partypuzl.data.local.entities.PartyEntity
import com.restrusher.partypuzl.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzl.data.local.entities.PartyWithPlayers
import kotlinx.coroutines.flow.Flow

interface PartyProxy {
    suspend fun createParty(party: PartyEntity): Long
    suspend fun linkPlayerToParty(crossRef: PartyPlayerCrossRef)
    fun getAllParties(): Flow<List<PartyWithPlayers>>
    suspend fun getPartyById(partyId: Int): PartyWithPlayers?
    suspend fun updateLastUsed(partyId: Int, gameModeNameRes: Int)
    suspend fun updatePartyName(partyId: Int, name: String)
    suspend fun deleteParty(partyId: Int)
}
