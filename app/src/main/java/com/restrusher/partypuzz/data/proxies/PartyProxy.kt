package com.restrusher.partypuzz.data.proxies

import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef

interface PartyProxy {
    suspend fun createParty(party: PartyEntity): Long
    suspend fun linkPlayerToParty(crossRef: PartyPlayerCrossRef)
}
