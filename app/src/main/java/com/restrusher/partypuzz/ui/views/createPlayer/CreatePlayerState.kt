package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import com.restrusher.partypuzz.data.models.Gender

data class CreatePlayerState(
    val playerId: Int = -1,
    val playerName: String = "",
    val gender: Gender = Gender.Unknown,
    val capturedImageUri: Uri = Uri.EMPTY,
    val randomAvatarRes: Int? = null,
    val existingPhotoPath: String? = null,
    val isLoading: Boolean = false
) {
    val isEditMode: Boolean get() = playerId != -1
}