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
data class CreatePlayerScreen(val playerId: Int = -1, val isCouplesMode: Boolean = false)

@Serializable
data object LoadingScreen

@Serializable
data object GameScreen

@Serializable
data object PartiesScreen

@Serializable
data object SettingsScreen

@Serializable
data class PartyDetailScreen(val partyId: Int)

@Serializable
data class FollowTheSpotRoute(
    val player1Name: String,
    val player1PhotoPath: String?,
    val player1AvatarName: String?,
    val player2Name: String,
    val player2PhotoPath: String?,
    val player2AvatarName: String?
)

@Serializable
data object HotPotatoRoute
