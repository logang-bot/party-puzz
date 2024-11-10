package com.restrusher.partypuzz.ui.views.gameConfig

import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player

data class GameConfigState(
    val players: List<Player> = GamePlayersList.PlayersList
)