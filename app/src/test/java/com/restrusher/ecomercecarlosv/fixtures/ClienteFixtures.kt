package com.restrusher.ecomercecarlosv.fixtures

import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.Mercado

fun cliente(
    id: String = "cliente-1",
    name: String = "Doña Ana",
    mercadoId: String = "mercado-1",
    isBlacklisted: Boolean = false,
    blacklistBalance: Double = 0.0,
    blacklistIsManualAmount: Boolean = false,
): Cliente = Cliente(
    id = id,
    mercadoId = mercadoId,
    name = name,
    description = "Puesto 12",
    phones = listOf("70000001"),
    isBlacklisted = isBlacklisted,
    blacklistBalance = blacklistBalance,
    blacklistIsManualAmount = blacklistIsManualAmount,
    createdAt = 1_600_000_000_000L,
)

fun mercado(id: String = "mercado-1", name: String = "Mercado Central"): Mercado = Mercado(
    id = id,
    name = name,
    address = "Av. Siempre Viva 123",
    createdAt = 1_600_000_000_000L,
)

fun syncOperation(
    id: Long = 1L,
    entityType: String = "PEDIDO",
    operation: String = "UPSERT",
    entityLabel: String = "Pedido de Doña Ana",
    createdAt: Long = 1_700_000_000_000L,
    retryCount: Int = 0,
): SyncOperationEntity = SyncOperationEntity(
    id = id,
    entityType = entityType,
    entityId = "entidad-$id",
    operation = operation,
    createdAt = createdAt,
    retryCount = retryCount,
    entityLabel = entityLabel,
)
