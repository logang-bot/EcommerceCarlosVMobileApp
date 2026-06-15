package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import com.restrusher.ecomercecarlosv.data.remote.dto.DetallePedidoDto
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

    fun fromDto(dto: DetallePedidoDto) = DetallePedidoEntity(
        id = dto.id,
        pedidoId = dto.pedidoId,
        productoId = dto.productoId,
        productName = dto.productName,
        quantity = dto.quantity,
        unitPrice = dto.unitPrice,
        catalogPrice = dto.catalogPrice,
        notes = dto.notes,
    )

    fun toDto(entity: DetallePedidoEntity) = DetallePedidoDto(
        id = entity.id,
        pedidoId = entity.pedidoId,
        productoId = entity.productoId,
        productName = entity.productName,
        quantity = entity.quantity,
        unitPrice = entity.unitPrice,
        catalogPrice = entity.catalogPrice,
        notes = entity.notes,
    )
}
