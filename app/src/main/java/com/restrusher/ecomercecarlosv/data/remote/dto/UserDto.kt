package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: Replace with Supabase postgrest-kt SupabaseClient call — see docs/features/usuarios.md
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    @SerialName("photo_url")   val photoUrl: String?  = null,
    @SerialName("is_active")   val isActive: Boolean  = true,
    @SerialName("created_at")  val createdAt: Long,
    @SerialName("last_seen_at")        val lastSeenAt: Long?         = null,
    @SerialName("biometric_enabled_at") val biometricEnabledAt: Long? = null,
)
