package com.restrusher.partypuzz.data.proxies

import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

interface PlayerProxy {
    fun getPlayers(): Flow<List<PlayerEntity>>
    suspend fun createPlayer(player: PlayerEntity)
}