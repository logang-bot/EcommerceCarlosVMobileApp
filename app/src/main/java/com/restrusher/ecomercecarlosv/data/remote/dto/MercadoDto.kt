package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MercadoDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("maps_url") val mapsUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long = 0L,
)
