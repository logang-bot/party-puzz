package com.restrusher.partypuzl.ui.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.CameraBackdrop
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Full-screen camera that stays inside the app: live preview, then a review step before the
 * photo is handed to [onCaptured]. [overlay] draws on top of the live preview (future filters).
 */
@Composable
fun InAppCameraDialog(
    onCaptured: (Uri) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    var capturedUri by remember { mutableStateOf<Uri?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CameraBackdrop)
        ) {
            val photo = capturedUri
            if (photo == null) {
                LiveCameraContent(
                    onCaptured = { capturedUri = it },
                    onClose = onDismiss,
                    overlay = overlay
                )
            } else {
                PhotoReviewContent(
                    photo = photo,
                    onRetake = { capturedUri = null },
                    onUsePhoto = { onCaptured(photo) }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.LiveCameraContent(
    onCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
    overlay: @Composable BoxScope.() -> Unit
) {
    val controller = rememberCameraController()
    val scope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }

    CameraViewfinder(controller = controller, modifier = Modifier.fillMaxSize())
    overlay()
    CameraControls(
        isCapturing = isCapturing,
        onClose = onClose,
        onShutter = {
            isCapturing = true
            scope.launch {
                captureOrNull(controller)?.let(onCaptured)
                isCapturing = false
            }
        },
        onFlip = controller::flipLens,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
    )
}

@Composable
private fun BoxScope.PhotoReviewContent(photo: Uri, onRetake: () -> Unit, onUsePhoto: () -> Unit) {
    AsyncImage(
        model = photo,
        contentDescription = stringResource(R.string.camera_captured_photo),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    PhotoReviewControls(
        onRetake = onRetake,
        onUsePhoto = onUsePhoto,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
    )
}

private suspend fun captureOrNull(controller: CameraController): Uri? = try {
    controller.capture()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Exception) {
    null
}
