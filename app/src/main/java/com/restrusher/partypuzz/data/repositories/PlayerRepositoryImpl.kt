package com.restrusher.partypuzz.data.repositories

import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.proxies.PlayerProxy
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzz.di.DatabaseProxy
import kotlinx.coroutines.flow.Flow

class PlayerRepositoryImpl(
    @DatabaseProxy private val playerLocalProxy: PlayerProxy
) : PlayerRepository {

    override fun getPlayers(): Flow<List<PlayerEntity>> {
        return playerLocalProxy.getPlayers()
    }

    override suspend fun createPlayer(player: PlayerEntity) {
        playerLocalProxy.createPlayer(player)
    }

    override suspend fun deletePlayer() {
        TODO("Not yet implemented")
    }

    override suspend fun editPlayer() {
        TODO("Not yet implemented")
    }

}