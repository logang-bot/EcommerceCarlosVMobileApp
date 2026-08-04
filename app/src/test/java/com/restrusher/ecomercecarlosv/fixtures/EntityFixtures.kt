package com.restrusher.ecomercecarlosv.fixtures

import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PagoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity

// Every optional field is given a distinct non-null value so a round-trip that silently drops one
// fails instead of matching a default.

fun clienteEntity(
    id: String = "cliente-1",
    phones: String = "70000001|70000002",
    primaryPhoneIndex: Int = 1,
    blacklistBalance: Double = 250.0,
    blacklistIsManualAmount: Boolean = true,
    isDeleted: Boolean = false,
) = ClienteEntity(
    id = id,
    mercadoId = "mercado-1",
    name = "Doña Ana",
    description = "Puesto 12",
    photoUrl = "https://example.test/ana.jpg",
    phones = phones,
    primaryPhoneIndex = primaryPhoneIndex,
    mapsUrl = "https://maps.google.com/?q=-16.5,-68.15",
    isBlacklisted = true,
    blacklistReason = "No paga hace meses",
    blacklistedAt = 1_700_000_000_000L,
    blacklistBalance = blacklistBalance,
    blacklistIsManualAmount = blacklistIsManualAmount,
    createdAt = 1_600_000_000_000L,
    updatedAt = 1_700_000_100_000L,
    isDeleted = isDeleted,
)

fun mercadoEntity(id: String = "mercado-1") = MercadoEntity(
    id = id,
    name = "Mercado Central",
    address = "Av. Siempre Viva 123",
    photoUrl = "https://example.test/mercado.jpg",
    mapsUrl = "https://maps.google.com/?q=-16.5,-68.15",
    latitude = -16.5,
    longitude = -68.15,
    createdAt = 1_600_000_000_000L,
    updatedAt = 1_700_000_000_000L,
    isDeleted = false,
)

fun productoEntity(id: String = "producto-1") = ProductoEntity(
    id = id,
    name = "Arroz",
    description = "Bolsa de 5 kg",
    price = 32.5,
    photoUrl = "https://example.test/arroz.jpg",
    isActive = true,
    createdAt = 1_600_000_000_000L,
    updatedAt = 1_700_000_000_000L,
    isDeleted = false,
)

fun pedidoEntity(
    id: String = "pedido-1",
    status: String = "PARTIAL",
    itemCount: Int = 3,
) = PedidoEntity(
    id = id,
    clienteId = "cliente-1",
    status = status,
    total = 120.0,
    paid = 45.0,
    notes = "Entregar el martes",
    createdAt = 1_600_000_000_000L,
    paidAt = 1_700_000_000_000L,
    isSaldoExtra = false,
    itemCount = itemCount,
    updatedAt = 1_700_000_100_000L,
    isDeleted = false,
)

fun detallePedidoEntity(
    id: String = "detalle-1",
    pedidoId: String = "pedido-1",
    productName: String = "Arroz",
    quantity: Int = 2,
) = DetallePedidoEntity(
    id = id,
    pedidoId = pedidoId,
    productoId = "producto-1",
    productName = productName,
    quantity = quantity,
    unitPrice = 30.0,
    catalogPrice = 32.5,
    notes = "Sin bolsa",
)

fun pagoEntity(id: String = "pago-1") = PagoEntity(
    id = id,
    pedidoId = "pedido-1",
    amount = 45.0,
    paidAt = 1_700_000_000_000L,
)

fun umbralesEntity(
    montoMaximo: Double = 500.0,
    diasMaximos: Int = 45,
) = UmbralesEntity(
    id = UmbralesEntity.SINGLETON_ID,
    montoMaximo = montoMaximo,
    diasMaximos = diasMaximos,
    updatedAt = 1_700_000_000_000L,
)

fun userEntity(
    id: String = "user-1",
    role: String = "SUPERUSUARIO",
    biometricEnabledAt: Long? = 1_700_000_000_000L,
) = UserEntity(
    id = id,
    email = "carlos@example.test",
    name = "Carlos Vargas",
    role = role,
    phone = "70000003",
    photoUrl = "https://example.test/carlos.jpg",
    isActive = true,
    createdAt = 1_600_000_000_000L,
    lastSeenAt = 1_700_000_050_000L,
    biometricEnabledAt = biometricEnabledAt,
)
