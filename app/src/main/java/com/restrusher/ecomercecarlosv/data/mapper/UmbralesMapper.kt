package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.data.remote.dto.UmbralesDto
import com.restrusher.ecomercecarlosv.domain.model.Umbrales

object UmbralesMapper {
    fun toDomain(entity: UmbralesEntity) = Umbrales(
        montoMaximo = entity.montoMaximo,
        diasMaximos = entity.diasMaximos,
    )

    fun toEntity(domain: Umbrales) = UmbralesEntity(
        montoMaximo = domain.montoMaximo,
        diasMaximos = domain.diasMaximos,
    )

    fun fromDto(dto: UmbralesDto) = UmbralesEntity(
        id = dto.id,
        montoMaximo = dto.montoMaximo,
        diasMaximos = dto.diasMaximos,
        updatedAt = dto.updatedAt,
    )

    fun toDto(entity: UmbralesEntity) = UmbralesDto(
        id = entity.id,
        montoMaximo = entity.montoMaximo,
        diasMaximos = entity.diasMaximos,
    )
}
