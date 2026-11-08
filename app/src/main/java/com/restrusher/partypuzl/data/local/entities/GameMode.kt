package com.restrusher.partypuzl.data.local.entities

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R

/**
 * The game modes a party can be played in, persisted by [name].
 *
 * Resource ids are renumbered whenever strings change, so only the enum name is stored; the
 * label is looked up from [nameRes] at runtime and always points at the current build's string.
 * [dealsSips] marks the modes whose outcomes can tell a player to take a sip — Bar Time, and
 * Party Puzl because it mixes Bar events in.
 */
enum class GameMode(@StringRes val nameRes: Int, val dealsSips: Boolean) {
    STANDARD(R.string.standard_game_mode, dealsSips = false),
    BAR(R.string.bar_game_mode, dealsSips = true),
    COUPLES(R.string.couples_game_mode, dealsSips = false),
    PARTY_PUZL(R.string.party_puzz_game_mode, dealsSips = true);

    companion object {
        fun fromKey(key: String?): GameMode? = entries.firstOrNull { it.name == key }

        fun fromNameRes(@StringRes nameRes: Int?): GameMode? =
            entries.firstOrNull { it.nameRes == nameRes }
    }
}
