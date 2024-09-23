package com.restrusher.partypuzz.data.appDataSource

import com.restrusher.partypuzz.data.models.Player

// TODO: This class and functionality needs to be migrated to ROOM
object GamePlayersList {
    var PlayerList: MutableList<Player> = mutableListOf()

    fun addPlayer(player: Player) {
        PlayerList.add(player)
    }
}