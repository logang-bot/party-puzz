package com.restrusher.partypuzz.ui.views.createPlayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CreatePlayerScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: CreatePlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainActivity = context.getActivity()
    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()) { perms ->
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
        contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) viewModel.onCapturedImage(uri)
    }

    setAppBarTitle(stringResource(id = R.string.create_player))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ImageOptionsContainer(
                takePictureAction = {
                    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isCameraPermissionGranted)
                        cameraLauncher.launch(uri)
                    else
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                },
                generateRandomImageAction = {
                    val index = viewModel.randomAvatarIndex()
                    val resId = context.resources.getIdentifier(
                        "img_dummy_avatar_$index", "drawable", context.packageName
                    )
                    viewModel.onRandomAvatarRequested(if (resId != 0) resId else null)
                },
                modifier = Modifier
                    .padding(24.dp))
            EditPlayerCard(uiState.capturedImageUri, uiState.playerName, avatarRes = uiState.randomAvatarRes)
            NameOptionsContainer(
                value = uiState.playerName,
                onValueChanged = { viewModel.onPlayerNameChanged(it) },
                onGenerateRandomName = {
                    viewModel.onPlayerNameChanged(viewModel.generateRandomName())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp))
        }

        Button(onClick = {

        }, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)) {
            Text(text = stringResource(id = R.string.confirm).uppercase(), style = MaterialTheme.typography.headlineSmall)
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun CreatePlayerScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CreatePlayerScreen({}, animatedVisibilityScope = this)
            }
        }
    }
}