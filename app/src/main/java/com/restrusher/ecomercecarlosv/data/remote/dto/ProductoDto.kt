package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long = 0L,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
