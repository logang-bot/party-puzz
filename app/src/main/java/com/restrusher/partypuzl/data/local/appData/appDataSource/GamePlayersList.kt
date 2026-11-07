package com.restrusher.partypuzl.data.local.appData.appDataSource

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.restrusher.partypuzl.data.models.Player

/**
 * The running game's roster, in memory only. The players themselves are already saved in Room as
 * the party's members; this is just the session's working copy. If Android kills the process the
 * app reopens on Home rather than rebuilding a game from it — see `ui/ProcessRestart`.
 */
object GamePlayersList {
    var PlayersList: SnapshotStateList<Player> = mutableStateListOf()
    var currentPartyId: Int? = null

    fun addPlayer(player: Player) {
        PlayersList.add(player)
    }

    fun setBaseNumberOfPlayers(numberOfPlayers: Int) {
        resetPlayersList()
        repeat(numberOfPlayers) {
            PlayersList.add(Player.getEmptyPlayer())
        }
    }

    fun removePlayer(playerId: Int) {
        PlayersList.removeIf { it.id == playerId }
    }

    fun updatePlayer(player: Player) {
        val index = PlayersList.indexOfFirst { it.id == player.id }
        if (index != -1) PlayersList[index] = player
    }

    fun resetPlayersList() {
        PlayersList.clear()
        currentPartyId = null
    }
}
