package com.restrusher.ecomercecarlosv.ui.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
    val file = File(imagesDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun copyImageToCache(context: Context, uri: Uri): Uri? {
    return try {
        val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
        val file = File(imagesDir, "gallery_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) { null }
}
