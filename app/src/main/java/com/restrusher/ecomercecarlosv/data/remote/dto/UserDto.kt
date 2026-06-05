package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This DTO maps to the `users` table in Supabase (PostgREST / realtime-kt).
//
// TODO (Phase 9): Wire all reads/writes through SupabaseClient instead of Room-only:
//   - Fetch:  supabaseClient.from("users").select().decodeList<UserDto>()
//   - Upsert: supabaseClient.from("users").upsert(userDto)
//   - Delete: supabaseClient.from("users").delete().eq("id", userId)
//   On every mutation, also update the local Room cache so offline reads remain fast.
//
// TODO (Phase 9): Add the `phone` column to the Supabase `users` table and
//   enable RLS policies so users can only update their own row.
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String? = null, // TODO (Phase 9): add column to Supabase users table
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("last_seen_at") val lastSeenAt: Long? = null,
    @SerialName("biometric_enabled_at") val biometricEnabledAt: Long? = null,
)
