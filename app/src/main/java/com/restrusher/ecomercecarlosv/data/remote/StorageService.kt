package com.restrusher.ecomercecarlosv.data.remote

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context,
) {
    suspend fun uploadPhoto(bucket: String, entityId: String, localUri: Uri): String {
        val bytes = requireNotNull(context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }) {
            "Cannot read image from $localUri"
        }
        val path = "$entityId/photo.jpg"
        supabase.storage.from(bucket).upload(path, bytes) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }
        return supabase.storage.from(bucket).publicUrl(path)
    }
}
