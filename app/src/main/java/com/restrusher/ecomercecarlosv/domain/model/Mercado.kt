package com.restrusher.ecomercecarlosv.domain.model

data class Mercado(
    val id: String,
    val name: String,
    val address: String,
    val photoUrl: String? = null,
    val mapsUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long,
)
