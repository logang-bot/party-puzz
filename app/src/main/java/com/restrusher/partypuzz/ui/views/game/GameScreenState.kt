package com.restrusher.partypuzz.ui.views.game

import com.restrusher.partypuzz.data.models.Player

enum class GameDealPhase {
    IDLE, ANIMATING, PLAYER_NAME_REVEAL, PLAYER_PHOTO_REVEAL, CHALLENGE_SHOWN
}

data class GameScreenState(
    val players: List<Player> = emptyList(),
    val dealPhase: GameDealPhase = GameDealPhase.IDLE,
    val animatingName: String = "",
    val selectedPlayer: Player? = null
)
