package com.restrusher.partypuzz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: PartyPlayerCrossRef)

    @Transaction
    @Query("SELECT * FROM parties ORDER BY COALESCE(lastUsedAt, dateCreation) DESC")
    fun getAllPartiesWithPlayers(): Flow<List<PartyWithPlayers>>

    @Query("UPDATE parties SET lastUsedAt = :timestamp WHERE id = :partyId")
    suspend fun updateLastUsed(partyId: Int, timestamp: Long)
}
