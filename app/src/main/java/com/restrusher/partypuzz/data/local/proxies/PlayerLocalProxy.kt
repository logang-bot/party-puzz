package com.restrusher.partypuzz.data.local.proxies

import com.restrusher.partypuzz.data.local.dao.PlayerDao
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.proxies.PlayerProxy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerLocalProxy @Inject constructor(private val playerDao: PlayerDao) : PlayerProxy {
    override fun getPlayers(): Flow<List<PlayerEntity>> {
        return playerDao.getAllPlayers()
    }

    override suspend fun createPlayer(player: PlayerEntity): Long {
        return playerDao.insert(player)
    }

    override suspend fun updatePlayer(player: PlayerEntity) {
        playerDao.update(player)
    }

    override suspend fun deletePlayer(player: PlayerEntity) {
        playerDao.delete(player)
    }
}
