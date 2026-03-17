package com.restrusher.partypuzz.ui.views.createPlayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.restrusher.partypuzz.data.models.Gender
import android.content.pm.ActivityInfo
import androidx.compose.ui.Modifier
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.CameraPermissionTextProvider
import com.restrusher.partypuzz.ui.common.PermissionDialog
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.utils.getActivity
import com.restrusher.partypuzz.utils.openAppSettings
import java.io.File

var permissionsToRequest = arrayOf(
    Manifest.permission.CAMERA
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CreatePlayerScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreatePlayerViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainActivity = context.getActivity()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { navigateBack() }
    }

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsToRequest.forEach { permission ->
            viewModel.onPermissionResult(
                permission = permission,
                isGranted = perms[permission] == true
            )
        }
    }

    val uri = remember {
        val tempFile = File.createTempFile(
            "picture_${System.currentTimeMillis()}", ".png", context.cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }

        FileProvider.getUriForFile(
            context.applicationContext,
            "${context.packageName}.provider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onCapturedImage(uri)
    }

    setAppBarTitle(stringResource(id = R.string.create_player))

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .sharedBounds(
                    rememberSharedContentState(key = "bounds"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(
                        tween(
                            500, easing = FastOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        tween(
                            500, easing = FastOutSlowInEasing
                        )
                    ),
                    boundsTransform = BoundsTransform { _: Rect, _: Rect ->
                        tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    })
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            PlayerFormContent(
                capturedImageUri = uiState.capturedImageUri,
                playerName = uiState.playerName,
                avatarRes = uiState.randomAvatarRes,
                gender = uiState.gender,
                onTakePicture = {
                    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isCameraPermissionGranted)
                        cameraLauncher.launch(uri)
                    else
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                },
                onGenerateRandomImage = {
                    val index = viewModel.randomAvatarIndex()
                    val resId = context.resources.getIdentifier(
                        "img_dummy_avatar_$index", "drawable", context.packageName
                    )
                    viewModel.onRandomAvatarRequested(if (resId != 0) resId else null)
                },
                onPlayerNameChanged = viewModel::onPlayerNameChanged,
                onGenerateRandomName = { viewModel.onPlayerNameChanged(viewModel.generateRandomName()) },
                onGenderSelected = viewModel::onGenderSelected
            )

            Button(
                onClick = { viewModel.confirmPlayer() },
                enabled = uiState.playerName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.confirm).uppercase(),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        if (uiState.isLoading) {
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

    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                    else -> return@forEach
                },
                isPermanentlyDeclined = !mainActivity!!.shouldShowRequestPermissionRationale(
                    permission
                ),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = {
                    mainActivity.openAppSettings()
                }
            )
        }
}

@Composable
fun PlayerFormContent(
    capturedImageUri: Uri,
    playerName: String,
    avatarRes: Int?,
    gender: Gender,
    onTakePicture: () -> Unit,
    onGenerateRandomImage: () -> Unit,
    onPlayerNameChanged: (String) -> Unit,
    onGenerateRandomName: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        ImageOptionsContainer(
            takePictureAction = onTakePicture,
            generateRandomImageAction = onGenerateRandomImage,
            modifier = Modifier.padding(24.dp)
        )
        EditPlayerCard(capturedImageUri, playerName, avatarRes = avatarRes)
        AnimatedVisibility(visible = playerName.isBlank()) {
            Text(
                text = stringResource(R.string.name_is_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 6.dp)
            )
        }
        NameOptionsContainer(
            value = playerName,
            onValueChanged = onPlayerNameChanged,
            onGenerateRandomName = onGenerateRandomName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
        GenderOptionsContainer(
            selectedGender = gender,
            onGenderSelected = onGenderSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerFormContentPreview() {
    PartyPuzzTheme {
        PlayerFormContent(
            capturedImageUri = Uri.EMPTY,
            playerName = "Alex",
            avatarRes = null,
            gender = Gender.Unknown,
            onTakePicture = {},
            onGenerateRandomImage = {},
            onPlayerNameChanged = {},
            onGenerateRandomName = {},
            onGenderSelected = {}
        )
    }
}
