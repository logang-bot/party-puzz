package com.restrusher.partypuzz.ui.views.home

import com.restrusher.partypuzz.data.local.entities.PlayerEntity

private const val MAX_PLAYER_NAMES_SHOWN = 3

internal fun String.shortenedName(): String {
    val first = trim().split("\\s+".toRegex()).firstOrNull() ?: this
    return if (first.length > 10) "${first.take(10)}…" else first
}

/**
 * Returns the shortened display names for the first [MAX_PLAYER_NAMES_SHOWN] players and
 * the count of remaining players beyond that limit.
 */
internal fun playerNamesSlice(players: List<PlayerEntity>): Pair<List<String>, Int> {
    val displayed = players.take(MAX_PLAYER_NAMES_SHOWN).map { it.nickName.shortenedName() }
    return displayed to (players.size - displayed.size)
}
