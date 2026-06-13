package com.restrusher.ecomercecarlosv.domain.model

data class Cliente(
    val id: String,
    val mercadoId: String,
    val name: String,
    val description: String,
    val photoUrl: String? = null,
    val phones: List<String> = emptyList(),
    val primaryPhoneIndex: Int = 0,
    val mapsUrl: String? = null,
    val isBlacklisted: Boolean = false,
    val blacklistReason: String? = null,
    val blacklistedAt: Long? = null,
    val blacklistBalance: Double = 0.0,
    val blacklistIsManualAmount: Boolean = false,
    val createdAt: Long,
)
