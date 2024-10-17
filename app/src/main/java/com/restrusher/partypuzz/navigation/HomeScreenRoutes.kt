package com.restrusher.partypuzz.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen

@Serializable
data class GameConfigScreen(
    val gameModeName: Int,
    val gameModeImage: Int
)

@Serializable
data object CreatePlayerScreen