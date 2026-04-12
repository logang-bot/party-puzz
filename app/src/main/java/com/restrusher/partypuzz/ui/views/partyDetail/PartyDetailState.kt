package com.restrusher.partypuzz.ui.views.partyDetail

import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers

enum class DownloadResult { SUCCESS, FAILURE }

data class PartyDetailState(
    val party: PartyWithPlayers? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editedName: String = "",
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val navigateBack: Boolean = false,
    val photos: List<PartyPhotoEntity> = emptyList(),
    val viewerPhotoIndex: Int? = null,
    val downloadResult: DownloadResult? = null
)
