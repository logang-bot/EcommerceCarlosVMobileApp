package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.domain.model.Mercado

object MercadoMapper {
    fun toDomain(entity: MercadoEntity) = Mercado(
        id = entity.id,
        name = entity.name,
        address = entity.address,
        photoUrl = entity.photoUrl,
        mapsUrl = entity.mapsUrl,
        latitude = entity.latitude,
        longitude = entity.longitude,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Mercado) = MercadoEntity(
        id = domain.id,
        name = domain.name,
        address = domain.address,
        photoUrl = domain.photoUrl,
        mapsUrl = domain.mapsUrl,
        latitude = domain.latitude,
        longitude = domain.longitude,
        createdAt = domain.createdAt,
    )
}
