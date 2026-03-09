package com.restrusher.partypuzz.data.repositories.interfaces

interface PartyRepository {
    suspend fun createParty(name: String): Long
    suspend fun linkPlayerToParty(partyId: Int, playerId: Int)
}
