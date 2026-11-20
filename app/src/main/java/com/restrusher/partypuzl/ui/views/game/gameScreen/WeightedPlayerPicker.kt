package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.Player
import kotlin.random.Random

/**
 * Picks whose turn it is. Every pick is random, so a player can come up twice in a row, but each
 * player's odds grow with the turns they have waited: the weight is `1 + turnsWaited`. Nobody is
 * left out for long, and nobody is ever guaranteed a turn.
 */
class WeightedPlayerPicker(private val random: Random = Random.Default) {
    var turnCount = 0
        private set

    private val turnsWaited = mutableMapOf<Int, Int>()

    fun pick(players: List<Player>): Player? {
        if (players.isEmpty()) return null
        val picked = drawWeighted(players)
        recordPick(picked, players)
        return picked
    }

    /** The round a turn belongs to, counting one round per `playerCount` turns. */
    fun roundNumber(playerCount: Int): Int =
        (turnCount - 1).coerceAtLeast(0) / playerCount.coerceAtLeast(1) + 1

    /** Walks the cumulative weights until it passes a roll drawn from the total. */
    private fun drawWeighted(players: List<Player>): Player {
        var roll = random.nextInt(players.sumOf(::weightOf))
        for (player in players) {
            roll -= weightOf(player)
            if (roll < 0) return player
        }
        return players.last()
    }

    private fun weightOf(player: Player): Int = 1 + (turnsWaited[player.id] ?: turnCount)

    private fun recordPick(picked: Player, players: List<Player>) {
        players.forEach { turnsWaited[it.id] = (turnsWaited[it.id] ?: turnCount) + 1 }
        turnsWaited[picked.id] = 0
        turnCount++
    }
}
