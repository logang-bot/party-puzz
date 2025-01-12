package com.restrusher.partypuzz.data.local.proxies

import com.restrusher.partypuzz.data.local.dao.PlayerDao
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.proxies.PlayerProxy
import kotlinx.coroutines.flow.Flow

class PlayerLocalProxy(private val playerDao: PlayerDao) : PlayerProxy {
    override fun getPlayers(): Flow<List<PlayerEntity>> {
        return playerDao.getAllPlayers()
    }

    override suspend fun createPlayer(player: PlayerEntity) {
        playerDao.insert(player)
    }
}