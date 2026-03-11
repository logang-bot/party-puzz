package com.restrusher.partypuzz.data.proxies

import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import kotlinx.coroutines.flow.Flow

interface PartyProxy {
    suspend fun createParty(party: PartyEntity): Long
    suspend fun linkPlayerToParty(crossRef: PartyPlayerCrossRef)
    fun getAllParties(): Flow<List<PartyWithPlayers>>
    suspend fun updateLastUsed(partyId: Int)
}
