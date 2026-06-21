package com.restrusher.ecomercecarlosv.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.restrusher.ecomercecarlosv.di.AdminClient
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    @AdminClient private val adminClient: SupabaseClient,
    @ApplicationContext private val context: Context,
) {
    suspend fun uploadPhoto(bucket: String, entityId: String, localUri: Uri): String {
        val bytes = requireNotNull(context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }) {
            "Cannot read image from $localUri"
        }
        require(bytes.isNotEmpty()) { "Image at $localUri is empty" }
        val path = "$entityId/photo.jpg"
        Log.d(TAG, "uploading $path to bucket '$bucket' (${bytes.size} bytes)")
        adminClient.storage.from(bucket).upload(path, bytes) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }
        val url = adminClient.storage.from(bucket).publicUrl(path)
        Log.d(TAG, "upload success → $url")
        return url
    }

    companion object {
        private const val TAG = "StorageService"
    }
}
