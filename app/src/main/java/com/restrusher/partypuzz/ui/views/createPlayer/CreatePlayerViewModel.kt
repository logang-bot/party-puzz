package com.restrusher.partypuzz.ui.views.createPlayer

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzz.data.models.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.restrusher.partypuzz.navigation.CreatePlayerScreen as CreatePlayerRoute

private const val MAX_RANDOM_AVATARS = 9

@HiltViewModel
class CreatePlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val partyRepository: PartyRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(CreatePlayerState())
    val uiState: StateFlow<CreatePlayerState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<Unit>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        val route = savedStateHandle.toRoute<CreatePlayerRoute>()
        val playerId = route.playerId
        val isCouplesMode = route.isCouplesMode
        _uiState.update {
            it.copy(
                isCouplesMode = isCouplesMode,
                interestedIn = if (!isCouplesMode) InterestedIn.Both else null,
                gender = if (!isCouplesMode) Gender.Unknown else null
            )
        }
        if (playerId != -1) {
            val player = GamePlayersList.PlayersList.firstOrNull { it.id == playerId }
            if (player != null) {
                val avatarRes = player.avatarName?.let {
                    val resId = context.resources.getIdentifier(it, "drawable", context.packageName)
                    if (resId != 0) resId else null
                }
                _uiState.update {
                    it.copy(
                        playerId = playerId,
                        playerName = player.nickName,
                        gender = if (isCouplesMode) player.gender else Gender.Unknown,
                        interestedIn = if (isCouplesMode) player.interestedIn else InterestedIn.Both,
                        randomAvatarRes = avatarRes,
                        existingPhotoPath = player.photoPath
                    )
                }
            }
        }
    }

    fun onPlayerNameChanged(name: String) {
        _uiState.update { it.copy(playerName = name) }
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onInterestedInSelected(interestedIn: InterestedIn) {
        _uiState.update { it.copy(interestedIn = interestedIn) }
    }

    fun onCapturedImage(uri: Uri) {
        _uiState.update { it.copy(capturedImageUri = uri, randomAvatarRes = null, existingPhotoPath = null) }
    }

    fun onRandomAvatarRequested(resId: Int?) {
        _uiState.update { it.copy(randomAvatarRes = resId, capturedImageUri = Uri.EMPTY, existingPhotoPath = null) }
    }

    fun randomAvatarIndex(): Int = (1..MAX_RANDOM_AVATARS).random()

    fun generateRandomPartyName(): String {
        val firstWords = context.resources.getStringArray(R.array.party_name_first_words)
        val secondWords = context.resources.getStringArray(R.array.party_name_second_words)
        return "${firstWords.random()} ${secondWords.random()}"
    }

    fun generateRandomName(): String {
        val adjectives = context.resources.getStringArray(R.array.name_adjectives)
        val nouns = context.resources.getStringArray(R.array.name_nouns)
        val wordCount = (2..3).random()
        val adj = adjectives.random()
        val noun = nouns.random()
        return if (wordCount == 2) {
            "$adj $noun"
        } else {
            "${adjectives.random()} $adj $noun"
        }
    }

    fun confirmPlayer() {
        if (_uiState.value.isEditMode) updatePlayer() else createPlayer()
    }

    private fun createPlayer() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val photoPath: String? = if (state.capturedImageUri != Uri.EMPTY) {
                copyPhotoToFilesDir(state.capturedImageUri)
            } else null

            val avatarName: String? = if (state.randomAvatarRes != null) {
                context.resources.getResourceEntryName(state.randomAvatarRes)
            } else null

            val playerId = playerRepository.createPlayer(
                PlayerEntity(
                    nickName = state.playerName,
                    gender = state.gender ?: Gender.Unknown,
                    interestedIn = state.interestedIn!!,
                    photoPath = photoPath,
                    avatarName = avatarName
                )
            )

            val partyId = GamePlayersList.currentPartyId
                ?: partyRepository.createParty(generateRandomPartyName()).toInt()
                    .also { GamePlayersList.currentPartyId = it }

            partyRepository.linkPlayerToParty(partyId, playerId.toInt())

            withContext(Dispatchers.Main) {
                GamePlayersList.addPlayer(
                    Player(
                        id = playerId.toInt(),
                        nickName = state.playerName,
                        gender = state.gender ?: Gender.Unknown,
                        interestedIn = state.interestedIn!!,
                        photoPath = photoPath,
                        avatarName = avatarName
                    )
                )
            }

            _uiState.update { it.copy(isLoading = false) }
            _navigationEvents.send(Unit)
        }
    }

    private fun updatePlayer() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val photoPath: String? = when {
                state.capturedImageUri != Uri.EMPTY -> copyPhotoToFilesDir(state.capturedImageUri)
                state.randomAvatarRes == null && state.existingPhotoPath != null -> state.existingPhotoPath
                else -> null
            }

            val avatarName: String? = if (state.randomAvatarRes != null) {
                context.resources.getResourceEntryName(state.randomAvatarRes)
            } else null

            playerRepository.editPlayer(
                PlayerEntity(
                    id = state.playerId,
                    nickName = state.playerName,
                    gender = state.gender ?: Gender.Unknown,
                    interestedIn = state.interestedIn!!,
                    photoPath = photoPath,
                    avatarName = avatarName
                )
            )

            withContext(Dispatchers.Main) {
                GamePlayersList.updatePlayer(
                    Player(
                        id = state.playerId,
                        nickName = state.playerName,
                        gender = state.gender ?: Gender.Unknown,
                        interestedIn = state.interestedIn!!,
                        photoPath = photoPath,
                        avatarName = avatarName
                    )
                )
            }

            _uiState.update { it.copy(isLoading = false) }
            _navigationEvents.send(Unit)
        }
    }

    private fun copyPhotoToFilesDir(sourceUri: Uri): String {
        val dir = File(context.filesDir, "player_photos").also { it.mkdirs() }
        val dest = File(dir, "player_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(sourceUri)?.use { it.copyTo(dest.outputStream()) }
        return dest.absolutePath
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeAt(0)
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}
