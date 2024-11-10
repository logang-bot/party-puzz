package com.restrusher.partypuzz.data.repositories

import com.restrusher.partypuzz.data.proxies.PlayerProxy
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzz.di.DatabaseProxy

class PlayerRepositoryImpl(
    @DatabaseProxy private val playerLocalProxy: PlayerProxy
) : PlayerRepository {

    override fun getPlayers() {
        TODO("Not yet implemented")

    }

}