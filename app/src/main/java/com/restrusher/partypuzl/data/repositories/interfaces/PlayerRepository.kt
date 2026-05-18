package com.restrusher.partypuzl.data.repositories.interfaces

import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayers(): Flow<List<PlayerEntity>>
    suspend fun createPlayer(player: PlayerEntity): Long
    suspend fun editPlayer(player: PlayerEntity)
    suspend fun deletePlayer(player: PlayerEntity)
}