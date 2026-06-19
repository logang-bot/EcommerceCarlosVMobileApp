package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.data.remote.dto.ProductoDto
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

    fun fromDto(dto: ProductoDto) = ProductoEntity(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        price = dto.price,
        photoUrl = dto.photoUrl,
        isActive = dto.isActive,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        isDeleted = dto.isDeleted,
    )

    fun toDto(entity: ProductoEntity) = ProductoDto(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        price = entity.price,
        photoUrl = entity.photoUrl,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        isDeleted = entity.isDeleted,
    )
}
