package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn

data class CreatePlayerState(
    val playerId: Int = -1,
    val playerName: String = "",
    val gender: Gender? = null,
    val interestedIn: InterestedIn? = null,
    val capturedImageUri: Uri = Uri.EMPTY,
    val randomAvatarRes: Int? = null,
    val existingPhotoPath: String? = null,
    val isLoading: Boolean = false,
    val isCouplesMode: Boolean = false
) {
    val isEditMode: Boolean get() = playerId != -1
}