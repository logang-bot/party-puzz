package com.restrusher.partypuzl.data.repositories

import com.restrusher.partypuzl.data.local.entities.PartyEntity
import com.restrusher.partypuzl.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzl.data.proxies.PartyProxy
import com.restrusher.partypuzl.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzl.di.DatabaseProxy

class PartyRepositoryImpl(@DatabaseProxy private val proxy: PartyProxy) : PartyRepository {
    override suspend fun createParty(name: String): Long =
        proxy.createParty(PartyEntity(name = name))

    override suspend fun linkPlayerToParty(partyId: Int, playerId: Int) =
        proxy.linkPlayerToParty(PartyPlayerCrossRef(partyId, playerId))

    override fun getAllParties() = proxy.getAllParties()

    override suspend fun getPartyById(partyId: Int) = proxy.getPartyById(partyId)

    override suspend fun updateLastUsed(partyId: Int, gameModeNameRes: Int) =
        proxy.updateLastUsed(partyId, gameModeNameRes)
    override suspend fun updatePartyName(partyId: Int, name: String) = proxy.updatePartyName(partyId, name)
    override suspend fun deleteParty(partyId: Int) = proxy.deleteParty(partyId)
}
