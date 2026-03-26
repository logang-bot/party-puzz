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

    @Transaction
    @Query("SELECT * FROM parties WHERE id = :partyId")
    suspend fun getPartyById(partyId: Int): PartyWithPlayers?

    @Query("UPDATE parties SET lastUsedAt = :timestamp WHERE id = :partyId")
    suspend fun updateLastUsed(partyId: Int, timestamp: Long)

    @Query("UPDATE parties SET name = :name WHERE id = :partyId")
    suspend fun updatePartyName(partyId: Int, name: String)

    @Query("DELETE FROM parties WHERE id = :partyId")
    suspend fun deleteParty(partyId: Int)

    @Query("DELETE FROM party_player_cross_ref WHERE partyId = :partyId")
    suspend fun deletePartyPlayerCrossRefs(partyId: Int)
}
