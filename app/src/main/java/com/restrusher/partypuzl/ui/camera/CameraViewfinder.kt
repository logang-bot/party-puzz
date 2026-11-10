package com.restrusher.partypuzl.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun rememberCameraController(): CameraController {
    val context = LocalContext.current
    return remember { CameraController(context.applicationContext) }
}

/** Live preview for [controller]; binds the camera while on screen and releases it when it leaves. */
@Composable
fun CameraViewfinder(controller: CameraController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(controller, lifecycleOwner) {
        controller.bind(lifecycleOwner, previewView.surfaceProvider)
    }
    DisposableEffect(controller) {
        onDispose { controller.unbind() }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
