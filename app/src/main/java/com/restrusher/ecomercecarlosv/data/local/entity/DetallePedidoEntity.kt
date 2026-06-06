package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_pedido",
    foreignKeys = [ForeignKey(
        entity = PedidoEntity::class,
        parentColumns = ["id"],
        childColumns = ["pedidoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("pedidoId")],
)
data class DetallePedidoEntity(
    @PrimaryKey val id: String,
    val pedidoId: String,
    val productoId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val catalogPrice: Double,
    val notes: String?,
)
