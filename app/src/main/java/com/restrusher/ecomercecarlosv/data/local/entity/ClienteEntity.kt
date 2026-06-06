package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clientes",
    foreignKeys = [ForeignKey(
        entity = MercadoEntity::class,
        parentColumns = ["id"],
        childColumns = ["mercadoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [
        Index("mercadoId"),
        Index("name"),
        Index("isBlacklisted"),
    ],
)
data class ClienteEntity(
    @PrimaryKey val id: String,
    val mercadoId: String,
    val name: String,
    val description: String,
    val photoUrl: String?,
    val phones: String,
    val mapsUrl: String?,
    val isBlacklisted: Boolean,
    val blacklistReason: String?,
    val blacklistedAt: Long?,
    val blacklistBalance: Double,
    val createdAt: Long,
)
