package com.restrusher.partypuzz.data.repositories.interfaces

import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayers(): Flow<List<PlayerEntity>>
    suspend fun createPlayer(player: PlayerEntity)
    suspend fun deletePlayer()
    suspend fun editPlayer()
}