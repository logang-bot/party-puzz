package com.restrusher.partypuzl.ui.views.game.gameScreen

import com.restrusher.partypuzl.data.models.Player
import kotlin.random.Random

/**
 * Picks whose turn it is, one round at a time. Each round is a fresh shuffle of every player, so
 * everyone plays exactly once per round in a random order. A new round never opens with the
 * player who closed the previous one, so nobody plays twice in a row.
 */
class RoundPlayerPicker(private val random: Random = Random.Default) {
    var roundNumber = 0
        private set

    private val roundQueue = ArrayDeque<Player>()
    private var lastPicked: Player? = null

    fun pick(players: List<Player>): Player? {
        roundQueue.retainAll(players)
        if (roundQueue.isEmpty()) startRound(players)
        return roundQueue.removeFirstOrNull()?.also { lastPicked = it }
    }

    private fun startRound(players: List<Player>) {
        if (players.isEmpty()) return
        roundQueue.addAll(shuffledAvoidingRepeat(players))
        roundNumber++
    }

    private fun shuffledAvoidingRepeat(players: List<Player>): List<Player> {
        val order = players.shuffled(random).toMutableList()
        if (order.size > 1 && order.first() == lastPicked) {
            order.add(random.nextInt(1, order.size), order.removeAt(0))
        }
        return order
    }
}
