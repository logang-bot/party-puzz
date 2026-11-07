package com.restrusher.partypuzl.data.local.appData.appDataSource

// In memory only, like GamePlayersList: a process restart reopens on Home (see ui/ProcessRestart).
object GameOptionsSource {

    // Set by GameConfigScreen before navigating to the game; read by GameScreenViewModel at init
    var currentGameModeNameRes: Int? = null
}
