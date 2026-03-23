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

    override suspend fun createPlayer(player: PlayerEntity): Long {
        return playerLocalProxy.createPlayer(player)
    }

    override suspend fun editPlayer(player: PlayerEntity) {
        playerLocalProxy.updatePlayer(player)
    }

    override suspend fun deletePlayer(player: PlayerEntity) {
        playerLocalProxy.deletePlayer(player)
    }

}