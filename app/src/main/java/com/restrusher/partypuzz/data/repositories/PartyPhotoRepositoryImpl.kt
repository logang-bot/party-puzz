package com.restrusher.partypuzz.data.repositories

import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzz.data.proxies.PartyPhotoProxy
import com.restrusher.partypuzz.data.repositories.interfaces.PartyPhotoRepository
import com.restrusher.partypuzz.di.DatabaseProxy

class PartyPhotoRepositoryImpl(
    @DatabaseProxy private val proxy: PartyPhotoProxy
) : PartyPhotoRepository {
    override suspend fun addPhoto(partyId: Int, photoPath: String) {
        proxy.insert(PartyPhotoEntity(partyId = partyId, photoPath = photoPath))
    }

    override fun getPhotosForParty(partyId: Int) = proxy.getPhotosForParty(partyId)

    override suspend fun deletePhoto(photo: PartyPhotoEntity) = proxy.delete(photo)
}
