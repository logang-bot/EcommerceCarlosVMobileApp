package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedidos",
    foreignKeys = [ForeignKey(
        entity = ClienteEntity::class,
        parentColumns = ["id"],
        childColumns = ["clienteId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("clienteId"), Index("status"), Index("createdAt")],
)
data class PedidoEntity(
    @PrimaryKey val id: String,
    val clienteId: String,
    val status: String,
    val total: Double,
    val paid: Double,
    val notes: String?,
    val createdAt: Long,
    val paidAt: Long?,
    val isSaldoExtra: Boolean = false,
    val itemCount: Int = 0,
)
