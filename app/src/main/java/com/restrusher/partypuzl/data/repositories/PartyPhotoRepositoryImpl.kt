package com.restrusher.partypuzl.data.repositories

import com.restrusher.partypuzl.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzl.data.proxies.PartyPhotoProxy
import com.restrusher.partypuzl.data.repositories.interfaces.PartyPhotoRepository
import com.restrusher.partypuzl.di.DatabaseProxy

class PartyPhotoRepositoryImpl(
    @DatabaseProxy private val proxy: PartyPhotoProxy
) : PartyPhotoRepository {
    override suspend fun addPhoto(partyId: Int, photoPath: String) {
        proxy.insert(PartyPhotoEntity(partyId = partyId, photoPath = photoPath))
    }

    override fun getPhotosForParty(partyId: Int) = proxy.getPhotosForParty(partyId)

    override suspend fun deletePhoto(photo: PartyPhotoEntity) = proxy.delete(photo)
}
