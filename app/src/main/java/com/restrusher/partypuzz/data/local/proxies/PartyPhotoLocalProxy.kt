package com.restrusher.partypuzz.data.local.proxies

import com.restrusher.partypuzz.data.local.dao.PartyPhotoDao
import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzz.data.proxies.PartyPhotoProxy
import javax.inject.Inject

class PartyPhotoLocalProxy @Inject constructor(
    private val dao: PartyPhotoDao
) : PartyPhotoProxy {
    override suspend fun insert(photo: PartyPhotoEntity): Long = dao.insert(photo)
    override fun getPhotosForParty(partyId: Int) = dao.getPhotosForParty(partyId)
    override suspend fun delete(photo: PartyPhotoEntity) = dao.delete(photo)
}
