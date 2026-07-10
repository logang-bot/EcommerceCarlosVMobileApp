package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.PagoEntity
import com.restrusher.ecomercecarlosv.data.remote.dto.PagoDto
import com.restrusher.ecomercecarlosv.domain.model.Pago

object PagoMapper {
    fun toDomain(entity: PagoEntity) = Pago(
        id = entity.id,
        pedidoId = entity.pedidoId,
        amount = entity.amount,
        paidAt = entity.paidAt,
    )

    fun toEntity(domain: Pago) = PagoEntity(
        id = domain.id,
        pedidoId = domain.pedidoId,
        amount = domain.amount,
        paidAt = domain.paidAt,
    )

    fun fromDto(dto: PagoDto) = PagoEntity(
        id = dto.id,
        pedidoId = dto.pedidoId,
        amount = dto.amount,
        paidAt = dto.paidAt,
    )

    fun toDto(entity: PagoEntity) = PagoDto(
        id = entity.id,
        pedidoId = entity.pedidoId,
        amount = entity.amount,
        paidAt = entity.paidAt,
    )
}
