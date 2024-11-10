package com.restrusher.partypuzz.data.local.appData.appDataSource

import com.restrusher.partypuzz.data.models.Player

// TODO: This class and functionality needs to be migrated to ROOM
object GamePlayersList {
    var PlayersList: MutableList<Player> = mutableListOf()

    fun addPlayer(player: Player) {
        PlayersList.add(player)
    }

    fun setBaseNumberOfPlayers(numberOfPlayers: Int) {
        resetPlayersList()
        repeat(numberOfPlayers) {
            PlayersList.add(Player.getEmptyPlayer())
        }
    }

    fun resetPlayersList() {
        PlayersList.clear();
    }
}