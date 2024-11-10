package com.restrusher.partypuzz.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: PlayerEntity)

    @Update
    suspend fun update(player: PlayerEntity)

    @Delete
    suspend fun delete(player: PlayerEntity)

    @Query("SELECT * from players WHERE id = :id")
    fun getPlayer(id: Int): Flow<PlayerEntity>

    @Query("SELECT * from players ORDER BY nickName ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
}