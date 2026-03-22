package com.restrusher.partypuzz.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen

@Serializable
data class GameConfigScreen(
    val gameModeName: Int,
    val gameModeImage: Int,
    val gameModeDescription: Int,
    val partyId: Int? = null
)

@Serializable
data object CreatePlayerScreen

@Serializable
data object LoadingScreen

@Serializable
data object GameScreen

@Serializable
data class FollowTheSpotRoute(
    val player1Name: String,
    val player1PhotoPath: String?,
    val player1AvatarName: String?,
    val player2Name: String,
    val player2PhotoPath: String?,
    val player2AvatarName: String?
)
