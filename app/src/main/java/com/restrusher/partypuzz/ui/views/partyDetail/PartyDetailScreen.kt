package com.restrusher.partypuzz.ui.views.partyDetail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R

@Composable
fun PartyDetailScreen(
    partyId: Int,
    setAppBarTitle: (String) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PartyDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    var pendingDownloadPath by remember { mutableStateOf<String?>(null) }
    val writePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingDownloadPath?.let { viewModel.downloadPhoto(it) }
        pendingDownloadPath = null
    }

    val title = stringResource(id = R.string.party_detail)
    LaunchedEffect(Unit) {
        setAppBarTitle(title)
        viewModel.loadParty(partyId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) navigateBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.party != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                PartyNameSection(
                    name = uiState.party!!.party.name,
                    isEditing = uiState.isEditing,
                    editedName = uiState.editedName,
                    onEditClick = viewModel::startEditing,
                    onNameChange = viewModel::onNameChange,
                    onSave = viewModel::savePartyName,
                    onDiscard = viewModel::discardEditing,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartyPlayersGrid(
                    players = uiState.party!!.players,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartyPhotoAlbumSection(
                    photos = uiState.photos,
                    hasCameraPermission = hasCameraPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onPhotoClick = viewModel::openPhotoViewer,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                DeletePartyButton(
                    onClick = viewModel::showDeleteDialog,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.isSaving || uiState.isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    uiState.viewerPhotoIndex?.let { index ->
        PhotoViewerDialog(
            photos = uiState.photos,
            initialIndex = index,
            downloadResult = uiState.downloadResult,
            onDismiss = viewModel::closePhotoViewer,
            onDownloadResultConsumed = viewModel::clearDownloadResult,
            onDownload = { photoPath ->
                val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                if (needsPermission) {
                    pendingDownloadPath = photoPath
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    viewModel.downloadPhoto(photoPath)
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text(text = stringResource(id = R.string.delete_party_title)) },
            text = { Text(text = stringResource(id = R.string.delete_party_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        )
    }
}
