package com.restrusher.ecomercecarlosv.fixtures

import com.restrusher.ecomercecarlosv.data.remote.dto.ClienteDto
import com.restrusher.ecomercecarlosv.data.remote.dto.DetallePedidoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.MercadoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.PagoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.PedidoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.ProductoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.UmbralesDto
import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto

fun clienteDto(
    id: String = "cliente-1",
    phones: String? = "70000001|70000002",
    isDeleted: Boolean = false,
) = ClienteDto(
    id = id,
    mercadoId = "mercado-1",
    name = "Doña Ana",
    description = "Puesto 12",
    photoUrl = "https://example.test/ana.jpg",
    phones = phones,
    mapsUrl = "https://maps.google.com/?q=-16.5,-68.15",
    isBlacklisted = true,
    blacklistReason = "No paga hace meses",
    blacklistedAt = 1_700_000_000_000L,
    createdAt = 1_600_000_000_000L,
    updatedAt = 1_700_000_100_000L,
    isDeleted = isDeleted,
)

fun mercadoDto(id: String = "mercado-1") = MercadoDto(
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

fun productoDto(id: String = "producto-1") = ProductoDto(
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

fun pedidoDto(id: String = "pedido-1", status: String = "PARTIAL") = PedidoDto(
    id = id,
    clienteId = "cliente-1",
    status = status,
    total = 120.0,
    paid = 45.0,
    notes = "Entregar el martes",
    createdAt = 1_600_000_000_000L,
    paidAt = 1_700_000_000_000L,
    isSaldoExtra = false,
    updatedAt = 1_700_000_100_000L,
    isDeleted = false,
)

fun detallePedidoDto(id: String = "detalle-1") = DetallePedidoDto(
    id = id,
    pedidoId = "pedido-1",
    productoId = "producto-1",
    productName = "Arroz",
    quantity = 2,
    unitPrice = 30.0,
    catalogPrice = 32.5,
    notes = "Sin bolsa",
)

fun pagoDto(id: String = "pago-1") = PagoDto(
    id = id,
    pedidoId = "pedido-1",
    amount = 45.0,
    paidAt = 1_700_000_000_000L,
)

fun umbralesDto(montoMaximo: Double = 500.0, diasMaximos: Int = 45) = UmbralesDto(
    montoMaximo = montoMaximo,
    diasMaximos = diasMaximos,
    updatedAt = 1_700_000_000_000L,
)

fun userDto(id: String = "user-1", role: String = "SUPERUSUARIO") = UserDto(
    id = id,
    email = "carlos@example.test",
    name = "Carlos Vargas",
    role = role,
    phone = "70000003",
    photoUrl = "https://example.test/carlos.jpg",
    isActive = true,
    createdAt = 1_600_000_000_000L,
    lastSeenAt = 1_700_000_050_000L,
)
