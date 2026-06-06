package com.restrusher.ecomercecarlosv.domain.model

data class Producto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val photoUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
)
