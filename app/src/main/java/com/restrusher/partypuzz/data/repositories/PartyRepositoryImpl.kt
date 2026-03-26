package com.restrusher.partypuzz.data.repositories

import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzz.data.proxies.PartyProxy
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzz.di.DatabaseProxy

class PartyRepositoryImpl(@DatabaseProxy private val proxy: PartyProxy) : PartyRepository {
    override suspend fun createParty(name: String): Long =
        proxy.createParty(PartyEntity(name = name))

    override suspend fun linkPlayerToParty(partyId: Int, playerId: Int) =
        proxy.linkPlayerToParty(PartyPlayerCrossRef(partyId, playerId))

    override fun getAllParties() = proxy.getAllParties()

    override suspend fun getPartyById(partyId: Int) = proxy.getPartyById(partyId)

    override suspend fun updateLastUsed(partyId: Int) = proxy.updateLastUsed(partyId)
    override suspend fun updatePartyName(partyId: Int, name: String) = proxy.updatePartyName(partyId, name)
    override suspend fun deleteParty(partyId: Int) = proxy.deleteParty(partyId)
}
