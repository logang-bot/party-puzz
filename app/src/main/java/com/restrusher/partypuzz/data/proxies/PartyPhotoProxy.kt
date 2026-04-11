package com.restrusher.partypuzz.data.proxies

import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import kotlinx.coroutines.flow.Flow

interface PartyPhotoProxy {
    suspend fun insert(photo: PartyPhotoEntity): Long
    fun getPhotosForParty(partyId: Int): Flow<List<PartyPhotoEntity>>
    suspend fun delete(photo: PartyPhotoEntity)
}
