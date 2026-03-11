package com.restrusher.partypuzz.data.local.proxies

import com.restrusher.partypuzz.data.local.dao.PartyDao
import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzz.data.proxies.PartyProxy
import javax.inject.Inject

class PartyLocalProxy @Inject constructor(private val partyDao: PartyDao) : PartyProxy {
    override suspend fun createParty(party: PartyEntity): Long = partyDao.insertParty(party)
    override suspend fun linkPlayerToParty(crossRef: PartyPlayerCrossRef) = partyDao.insertCrossRef(crossRef)
    override fun getLastParty() = partyDao.getLastPartyWithPlayers()
}
