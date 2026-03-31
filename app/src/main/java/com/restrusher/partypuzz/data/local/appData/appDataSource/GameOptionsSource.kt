package com.restrusher.partypuzz.data.local.appData.appDataSource

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// TODO: Migrate to persistent storage alongside GamePlayersList
object GameOptionsSource {

    data class GameOption(@StringRes val labelRes: Int, val enabled: Boolean)

    val options: SnapshotStateList<GameOption> = mutableStateListOf()

    // Set by GameConfigScreen before navigating to the game; read by GameScreenViewModel at init
    var currentGameModeNameRes: Int? = null

    fun initialize(initial: List<GameOption>) {
        options.clear()
        options.addAll(initial)
    }

    fun toggle(labelRes: Int) {
        val index = options.indexOfFirst { it.labelRes == labelRes }
        if (index >= 0) options[index] = options[index].copy(enabled = !options[index].enabled)
    }
}
