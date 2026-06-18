package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mercados")
data class MercadoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val photoUrl: String?,
    val mapsUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: Long,
    val updatedAt: Long = 0L,
)
