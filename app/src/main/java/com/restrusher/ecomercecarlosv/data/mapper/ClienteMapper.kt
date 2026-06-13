package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import com.restrusher.ecomercecarlosv.domain.model.Cliente

object ClienteMapper {
    fun toDomain(entity: ClienteEntity) = Cliente(
        id = entity.id,
        mercadoId = entity.mercadoId,
        name = entity.name,
        description = entity.description,
        photoUrl = entity.photoUrl,
        phones = if (entity.phones.isBlank()) emptyList() else entity.phones.split("|"),
        primaryPhoneIndex = entity.primaryPhoneIndex,
        mapsUrl = entity.mapsUrl,
        isBlacklisted = entity.isBlacklisted,
        blacklistReason = entity.blacklistReason,
        blacklistedAt = entity.blacklistedAt,
        blacklistBalance = entity.blacklistBalance,
        blacklistIsManualAmount = entity.blacklistIsManualAmount,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Cliente) = ClienteEntity(
        id = domain.id,
        mercadoId = domain.mercadoId,
        name = domain.name,
        description = domain.description,
        photoUrl = domain.photoUrl,
        phones = domain.phones.joinToString("|"),
        primaryPhoneIndex = domain.primaryPhoneIndex,
        mapsUrl = domain.mapsUrl,
        isBlacklisted = domain.isBlacklisted,
        blacklistReason = domain.blacklistReason,
        blacklistedAt = domain.blacklistedAt,
        blacklistBalance = domain.blacklistBalance,
        blacklistIsManualAmount = domain.blacklistIsManualAmount,
        createdAt = domain.createdAt,
    )
}
