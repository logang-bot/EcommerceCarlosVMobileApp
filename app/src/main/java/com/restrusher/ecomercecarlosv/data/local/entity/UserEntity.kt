package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val role: String,          // UserRole enum name
    val photoUrl: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val lastSeenAt: Long?,
    val biometricEnabledAt: Long? = null,
)
