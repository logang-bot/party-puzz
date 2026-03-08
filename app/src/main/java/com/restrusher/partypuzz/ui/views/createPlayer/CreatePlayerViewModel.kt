package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.local.appData.appDataSource.WordBank
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_RANDOM_AVATARS = 7

class CreatePlayerViewModel : ViewModel() {

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(CreatePlayerState())
    val uiState: StateFlow<CreatePlayerState> = _uiState.asStateFlow()

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

    fun addPlayer(playerName: String, gender: Gender) {
        val player = Player(GamePlayersList.PlayersList.size, playerName, gender)
        GamePlayersList.addPlayer(player)
        _uiState.update { currentState ->
            currentState.copy(
                playerId = GamePlayersList.PlayersList.size,
                playerName = playerName,
                gender = gender
            )
        }
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