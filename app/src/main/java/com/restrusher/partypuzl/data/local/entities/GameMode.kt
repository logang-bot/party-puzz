package com.restrusher.partypuzl.data.local.entities

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R

/**
 * The game modes a party can be played in, persisted by [name].
 *
 * Resource ids are renumbered whenever strings change, so only the enum name is stored; the
 * label is looked up from [nameRes] at runtime and always points at the current build's string.
 */
enum class GameMode(@StringRes val nameRes: Int) {
    STANDARD(R.string.standard_game_mode),
    BAR(R.string.bar_game_mode),
    COUPLES(R.string.couples_game_mode),
    PARTY_PUZL(R.string.party_puzz_game_mode);

    companion object {
        fun fromKey(key: String?): GameMode? = entries.firstOrNull { it.name == key }

        fun fromNameRes(@StringRes nameRes: Int?): GameMode? =
            entries.firstOrNull { it.nameRes == nameRes }
    }
}
