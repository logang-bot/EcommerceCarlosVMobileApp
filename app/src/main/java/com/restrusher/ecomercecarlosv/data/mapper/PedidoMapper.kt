package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus

object PedidoMapper {
    fun toDomain(entity: PedidoEntity) = Pedido(
        id = entity.id,
        clienteId = entity.clienteId,
        status = PedidoStatus.valueOf(entity.status),
        total = entity.total,
        paid = entity.paid,
        notes = entity.notes,
        createdAt = entity.createdAt,
        paidAt = entity.paidAt,
        isSaldoExtra = entity.isSaldoExtra,
        itemCount = entity.itemCount,
    )

    fun toEntity(domain: Pedido) = PedidoEntity(
        id = domain.id,
        clienteId = domain.clienteId,
        status = domain.status.name,
        total = domain.total,
        paid = domain.paid,
        notes = domain.notes,
        createdAt = domain.createdAt,
        paidAt = domain.paidAt,
        isSaldoExtra = domain.isSaldoExtra,
        itemCount = domain.itemCount,
    )
}
