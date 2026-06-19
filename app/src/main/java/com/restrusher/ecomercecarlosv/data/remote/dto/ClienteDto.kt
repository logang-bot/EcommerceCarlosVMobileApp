package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: String,
    @SerialName("mercado_id") val mercadoId: String,
    val name: String,
    val description: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val phones: String? = null,
    @SerialName("maps_url") val mapsUrl: String? = null,
    @SerialName("is_blacklisted") val isBlacklisted: Boolean = false,
    @SerialName("blacklist_reason") val blacklistReason: String? = null,
    @SerialName("blacklisted_at") val blacklistedAt: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long = 0L,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
