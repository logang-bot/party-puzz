package com.restrusher.partypuzl.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.restrusher.partypuzl.utils.getActivity
import com.restrusher.partypuzl.utils.openAppSettings

fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

class CameraPermissionRequester internal constructor(private val onRequest: () -> Unit) {
    fun request() = onRequest()
}

/**
 * Runs `onGranted` straight away when the camera is already allowed, otherwise asks the system
 * first. A refusal brings up [PermissionDialog], which retries the request or — once the user has
 * declined for good — sends them to the app's system settings.
 */
@Composable
fun rememberCameraPermissionRequester(onGranted: () -> Unit): CameraPermissionRequester {
    val context = LocalContext.current
    val currentOnGranted by rememberUpdatedState(onGranted)
    var isDialogVisible by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) currentOnGranted() else isDialogVisible = true
    }

    if (isDialogVisible) {
        CameraPermissionDialog(
            onDismiss = { isDialogVisible = false },
            onRetry = { isDialogVisible = false; launcher.launch(Manifest.permission.CAMERA) }
        )
    }

    return remember(launcher) {
        CameraPermissionRequester {
            if (hasCameraPermission(context)) currentOnGranted()
            else launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
private fun CameraPermissionDialog(onDismiss: () -> Unit, onRetry: () -> Unit) {
    val activity = LocalContext.current.getActivity()
    PermissionDialog(
        permissionTextProvider = CameraPermissionTextProvider(),
        isPermanentlyDeclined = activity?.shouldShowRequestPermissionRationale(
            Manifest.permission.CAMERA
        ) == false,
        onDismiss = onDismiss,
        onOkClick = onRetry,
        onGoToAppSettingsClick = { onDismiss(); activity?.openAppSettings() }
    )
}
