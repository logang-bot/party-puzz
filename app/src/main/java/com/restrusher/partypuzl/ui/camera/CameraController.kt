package com.restrusher.partypuzl.ui.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.restrusher.partypuzl.utils.createTempPictureFile
import com.restrusher.partypuzl.utils.uriForCacheFile
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Owns the CameraX use cases behind the in-app camera and the lens they are bound to. */
class CameraController(private val context: Context) {
    var lensFacing by mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
        private set

    private val preview = Preview.Builder().build()
    private val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    private var provider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null

    suspend fun bind(owner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        preview.surfaceProvider = surfaceProvider
        lifecycleOwner = owner
        provider = awaitProvider()
        rebind()
    }

    fun flipLens() {
        lensFacing = oppositeLens()
        rebind()
    }

    suspend fun capture(): Uri {
        val file = context.createTempPictureFile()
        takePicture(outputOptions(file))
        return context.uriForCacheFile(file)
    }

    fun unbind() {
        provider?.unbindAll()
    }

    private suspend fun awaitProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({ continuation.resume(future.get()) }, ContextCompat.getMainExecutor(context))
        }

    private fun rebind() {
        val cameraProvider = provider ?: return
        val owner = lifecycleOwner ?: return
        if (!cameraProvider.hasCamera(selectorFor(lensFacing))) lensFacing = oppositeLens()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(owner, selectorFor(lensFacing), preview, imageCapture)
    }

    private fun oppositeLens(): Int =
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    private fun selectorFor(lens: Int): CameraSelector =
        CameraSelector.Builder().requireLensFacing(lens).build()

    private suspend fun takePicture(options: ImageCapture.OutputFileOptions) =
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(context)
            imageCapture.takePicture(options, executor, ImageSavedResumer(continuation))
        }

    private fun outputOptions(file: File): ImageCapture.OutputFileOptions {
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        return ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()
    }

    private class ImageSavedResumer(
        private val continuation: Continuation<Unit>
    ) : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) = continuation.resume(Unit)

        override fun onError(exception: ImageCaptureException) =
            continuation.resumeWithException(exception)
    }
}
