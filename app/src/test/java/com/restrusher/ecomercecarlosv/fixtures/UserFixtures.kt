package com.restrusher.ecomercecarlosv.fixtures

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.screen.pedido.CartItem

fun appUser(
    id: String = "user-1",
    name: String = "Carlos Vargas",
    role: UserRole = UserRole.USUARIO,
    isActive: Boolean = true,
    biometricEnabledAt: Long? = null,
): AppUser = AppUser(
    id = id,
    email = "$id@example.test",
    name = name,
    role = role,
    phone = "70000003",
    photoUrl = null,
    isActive = isActive,
    createdAt = 1_600_000_000_000L,
    lastSeenAt = null,
    biometricEnabledAt = biometricEnabledAt,
)

fun cartItem(
    productoId: String = "producto-1",
    productName: String = "Arroz",
    unitPrice: Double = 10.0,
    quantity: Int = 1,
    catalogPrice: Double = 10.0,
    notes: String? = null,
): CartItem = CartItem(
    productoId = productoId,
    productName = productName,
    unitPrice = unitPrice,
    catalogPrice = catalogPrice,
    quantity = quantity,
    notes = notes,
)
