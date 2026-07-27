package com.restrusher.partypuzl.data.local.appData.appDataSource

// TODO: Migrate to persistent storage alongside GamePlayersList
object GameOptionsSource {

    // Set by GameConfigScreen before navigating to the game; read by GameScreenViewModel at init
    var currentGameModeNameRes: Int? = null
}
