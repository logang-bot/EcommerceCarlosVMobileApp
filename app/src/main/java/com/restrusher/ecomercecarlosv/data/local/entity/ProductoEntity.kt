package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    indices = [Index("name")],
)
data class ProductoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val photoUrl: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false,
)
