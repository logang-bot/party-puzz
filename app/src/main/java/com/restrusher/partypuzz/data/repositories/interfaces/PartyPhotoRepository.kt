package com.restrusher.partypuzz.data.repositories.interfaces

import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import kotlinx.coroutines.flow.Flow

interface PartyPhotoRepository {
    suspend fun addPhoto(partyId: Int, photoPath: String)
    fun getPhotosForParty(partyId: Int): Flow<List<PartyPhotoEntity>>
    suspend fun deletePhoto(photo: PartyPhotoEntity)
}
