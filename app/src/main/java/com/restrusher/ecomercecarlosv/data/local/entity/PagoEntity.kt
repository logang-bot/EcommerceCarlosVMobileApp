package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [ForeignKey(
        entity = PedidoEntity::class,
        parentColumns = ["id"],
        childColumns = ["pedidoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("pedidoId")],
)
data class PagoEntity(
    @PrimaryKey val id: String,
    val pedidoId: String,
    val amount: Double,
    val paidAt: Long,
)
