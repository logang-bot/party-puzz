package com.restrusher.partypuzl.data.proxies

import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

interface PlayerProxy {
    fun getPlayers(): Flow<List<PlayerEntity>>
    suspend fun createPlayer(player: PlayerEntity): Long
    suspend fun updatePlayer(player: PlayerEntity)
    suspend fun deletePlayer(player: PlayerEntity)
}