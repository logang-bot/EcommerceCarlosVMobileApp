package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.restrusher.ecomercecarlosv.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

sealed interface SaveResult {
    data class Success(val fileName: String) : SaveResult
    data object NoSpace : SaveResult
    data class Error(val cause: Throwable) : SaveResult
}

suspend fun saveReportToDownloads(context: Context, html: String, fileName: String): SaveResult =
    withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/html")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("No se pudo crear el archivo")
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(html.toByteArray(Charsets.UTF_8))
                } ?: throw IOException("No se pudo abrir el archivo para escritura")
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                File(dir, fileName).writeText(html, Charsets.UTF_8)
            }
            SaveResult.Success(fileName)
        } catch (e: IOException) {
            val msg = e.message.orEmpty()
            if (msg.contains("ENOSPC") || msg.contains("No space left") || msg.contains("space")) {
                SaveResult.NoSpace
            } else {
                SaveResult.Error(e)
            }
        } catch (e: Exception) {
            SaveResult.Error(e)
        }
    }

fun showSaveToast(context: Context, result: SaveResult) {
    val msg = when (result) {
        is SaveResult.Success -> context.getString(R.string.reporte_guardado_ok)
        is SaveResult.NoSpace -> context.getString(R.string.reporte_guardado_sin_espacio)
        is SaveResult.Error -> context.getString(R.string.reporte_guardado_error)
    }
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}
