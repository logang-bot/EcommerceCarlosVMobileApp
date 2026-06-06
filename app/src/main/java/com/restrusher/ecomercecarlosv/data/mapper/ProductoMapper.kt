package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.domain.model.Producto

object ProductoMapper {
    fun toDomain(entity: ProductoEntity) = Producto(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        price = entity.price,
        photoUrl = entity.photoUrl,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Producto) = ProductoEntity(
        id = domain.id,
        name = domain.name,
        description = domain.description,
        price = domain.price,
        photoUrl = domain.photoUrl,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
    )
}
