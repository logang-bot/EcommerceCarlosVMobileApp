package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the `users` table in Supabase (PostgREST).
// biometricEnabledAt is intentionally absent — it is device-local only (see docs/db-schema.md).
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("last_seen_at") val lastSeenAt: Long? = null,
)
