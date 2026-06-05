package com.restrusher.ecomercecarlosv.domain.model

data class AppUser(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val phone: String? = null,
    val photoUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val lastSeenAt: Long? = null,
    val biometricEnabledAt: Long? = null,
)
