package com.restrusher.partypuzz.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyPhotoDao {

    @Insert
    suspend fun insert(photo: PartyPhotoEntity): Long

    @Query("SELECT * FROM party_photos WHERE partyId = :partyId ORDER BY takenAt DESC")
    fun getPhotosForParty(partyId: Int): Flow<List<PartyPhotoEntity>>

    @Delete
    suspend fun delete(photo: PartyPhotoEntity)
}
