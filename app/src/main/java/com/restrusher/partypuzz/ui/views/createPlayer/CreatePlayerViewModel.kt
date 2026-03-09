package com.restrusher.partypuzz.ui.views.createPlayer

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.local.appData.appDataSource.WordBank
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzz.data.repositories.interfaces.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val MAX_RANDOM_AVATARS = 7

@HiltViewModel
class CreatePlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val partyRepository: PartyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(CreatePlayerState())
    val uiState: StateFlow<CreatePlayerState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<Unit>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun onPlayerNameChanged(name: String) {
        _uiState.update { it.copy(playerName = name) }
    }

    fun onCapturedImage(uri: Uri) {
        _uiState.update { it.copy(capturedImageUri = uri, randomAvatarRes = null) }
    }

    fun onRandomAvatarRequested(resId: Int?) {
        _uiState.update { it.copy(randomAvatarRes = resId, capturedImageUri = Uri.EMPTY) }
    }

    fun randomAvatarIndex(): Int = (1..MAX_RANDOM_AVATARS).random()

    fun generateRandomName(): String {
        val wordCount = (2..3).random()
        val adj = WordBank.adjectives.random()
        val noun = WordBank.nouns.random()
        return if (wordCount == 2) {
            "$adj $noun"
        } else {
            "${WordBank.adjectives.random()} $adj $noun"
        }
    }

    fun confirmPlayer() {
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
                    gender = state.gender,
                    photoPath = photoPath,
                    avatarName = avatarName
                )
            )

            val partyId = GamePlayersList.currentPartyId
                ?: partyRepository.createParty("Party ${System.currentTimeMillis()}").toInt()
                    .also { GamePlayersList.currentPartyId = it }

            partyRepository.linkPlayerToParty(partyId, playerId.toInt())

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

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}
