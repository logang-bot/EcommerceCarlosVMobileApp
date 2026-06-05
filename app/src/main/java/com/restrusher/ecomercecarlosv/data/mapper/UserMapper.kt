package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity
import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole

object UserMapper {

    fun toDomain(entity: UserEntity) = AppUser(
        id                = entity.id,
        email             = entity.email,
        name              = entity.name,
        role              = UserRole.valueOf(entity.role),
        photoUrl          = entity.photoUrl,
        isActive          = entity.isActive,
        createdAt         = entity.createdAt,
        lastSeenAt        = entity.lastSeenAt,
        biometricEnabledAt = entity.biometricEnabledAt,
    )

    fun toEntity(domain: AppUser) = UserEntity(
        id                = domain.id,
        email             = domain.email,
        name              = domain.name,
        role              = domain.role.name,
        photoUrl          = domain.photoUrl,
        isActive          = domain.isActive,
        createdAt         = domain.createdAt,
        lastSeenAt        = domain.lastSeenAt,
        biometricEnabledAt = domain.biometricEnabledAt,
    )

    fun toDomain(dto: UserDto) = AppUser(
        id                = dto.id,
        email             = dto.email,
        name              = dto.name,
        role              = UserRole.valueOf(dto.role),
        photoUrl          = dto.photoUrl,
        isActive          = dto.isActive,
        createdAt         = dto.createdAt,
        lastSeenAt        = dto.lastSeenAt,
        biometricEnabledAt = dto.biometricEnabledAt,
    )

    fun toDto(domain: AppUser) = UserDto(
        id                = domain.id,
        email             = domain.email,
        name              = domain.name,
        role              = domain.role.name,
        photoUrl          = domain.photoUrl,
        isActive          = domain.isActive,
        createdAt         = domain.createdAt,
        lastSeenAt        = domain.lastSeenAt,
        biometricEnabledAt = domain.biometricEnabledAt,
    )
}
