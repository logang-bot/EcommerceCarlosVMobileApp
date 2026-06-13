package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PedidoWithLines(
    @Embedded val pedido: PedidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pedidoId",
    )
    val lines: List<DetallePedidoEntity>,
)
