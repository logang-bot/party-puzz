package com.restrusher.partypuzz.ui.views.partyDetail

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.repositories.interfaces.PartyPhotoRepository
import com.restrusher.partypuzz.data.repositories.interfaces.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PartyDetailViewModel @Inject constructor(
    private val partyRepository: PartyRepository,
    private val partyPhotoRepository: PartyPhotoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyDetailState())
    val uiState: StateFlow<PartyDetailState> = _uiState.asStateFlow()

    fun loadParty(partyId: Int) {
        if (_uiState.value.party != null) return
        viewModelScope.launch {
            val party = partyRepository.getPartyById(partyId)
            _uiState.update { it.copy(party = party, isLoading = false) }
        }
        partyPhotoRepository.getPhotosForParty(partyId)
            .onEach { photos -> _uiState.update { it.copy(photos = photos) } }
            .launchIn(viewModelScope)
    }

    fun startEditing() {
        val name = _uiState.value.party?.party?.name ?: return
        _uiState.update { it.copy(isEditing = true, editedName = name) }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(editedName = name) }

    fun discardEditing() = _uiState.update { it.copy(isEditing = false, editedName = "") }

    fun savePartyName() {
        val partyId = _uiState.value.party?.party?.id ?: return
        val newName = _uiState.value.editedName.trim()
        if (newName.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            partyRepository.updatePartyName(partyId, newName)
            val updated = partyRepository.getPartyById(partyId)
            _uiState.update { it.copy(party = updated, isSaving = false, isEditing = false, editedName = "") }
        }
    }

    fun openPhotoViewer(index: Int) = _uiState.update { it.copy(viewerPhotoIndex = index) }
    fun closePhotoViewer() = _uiState.update { it.copy(viewerPhotoIndex = null) }

    fun downloadPhoto(photoPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val sourceFile = File(photoPath)
                check(sourceFile.exists())
                val filename = "PartyPuzz_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PartyPuzz")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
                } else {
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                } ?: error("Failed to create MediaStore entry")
                resolver.openOutputStream(uri)?.use { out -> sourceFile.inputStream().copyTo(out) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            val downloadResult = if (result.isSuccess) DownloadResult.SUCCESS else DownloadResult.FAILURE
            _uiState.update { it.copy(downloadResult = downloadResult) }
        }
    }

    fun clearDownloadResult() = _uiState.update { it.copy(downloadResult = null) }

    fun showDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = true) }

    fun dismissDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        val partyId = _uiState.value.party?.party?.id ?: return
        val photoPaths = _uiState.value.photos.map { it.photoPath }
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }
            withContext(Dispatchers.IO) {
                photoPaths.forEach { path -> File(path).delete() }
                partyRepository.deleteParty(partyId)
            }
            _uiState.update { it.copy(isDeleting = false, navigateBack = true) }
        }
    }
}
