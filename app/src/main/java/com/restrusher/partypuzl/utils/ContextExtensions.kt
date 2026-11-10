package com.restrusher.partypuzl.utils

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.restrusher.partypuzl.BuildConfig
import java.io.File

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.createTempPictureFile(fileName: String = "picture_${System.currentTimeMillis()}"): File =
    File.createTempFile(fileName, ".jpg", cacheDir)

fun Context.uriForCacheFile(file: File): Uri =
    FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", file)
