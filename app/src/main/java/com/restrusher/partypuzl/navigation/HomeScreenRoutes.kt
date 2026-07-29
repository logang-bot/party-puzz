package com.restrusher.partypuzl.navigation

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

/** The custom-pack manager, reached from Settings. */
@Serializable
data object CustomPacksRoute

/** Create a pack when [packId] is null, edit its name/category/spice/description otherwise. */
@Serializable
data class CreateCustomPackRoute(val packId: String? = null)

/** A pack's contents — the list of authored entries. */
@Serializable
data class CustomPackEditorRoute(val packId: String)

/** Write one entry; [entryId] is null for a new one. */
@Serializable
data class CreateCustomEntryRoute(val packId: String, val entryId: String? = null)

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

@Serializable
data object SimonSaysRoute

@Serializable
data object CircleMasterRoute

@Serializable
data class TapWarRoute(
    val player1Name: String,
    val player1PhotoPath: String?,
    val player1AvatarName: String?,
    val player2Name: String,
    val player2PhotoPath: String?,
    val player2AvatarName: String?
)
