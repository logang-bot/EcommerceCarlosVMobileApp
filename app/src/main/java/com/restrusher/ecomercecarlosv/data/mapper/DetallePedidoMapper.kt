package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido

object DetallePedidoMapper {
    fun toDomain(entity: DetallePedidoEntity) = DetallePedido(
        id = entity.id,
        pedidoId = entity.pedidoId,
        productoId = entity.productoId,
        productName = entity.productName,
        quantity = entity.quantity,
        unitPrice = entity.unitPrice,
        catalogPrice = entity.catalogPrice,
        notes = entity.notes,
    )

    fun toEntity(domain: DetallePedido) = DetallePedidoEntity(
        id = domain.id,
        pedidoId = domain.pedidoId,
        productoId = domain.productoId,
        productName = domain.productName,
        quantity = domain.quantity,
        unitPrice = domain.unitPrice,
        catalogPrice = domain.catalogPrice,
        notes = domain.notes,
    )
}
