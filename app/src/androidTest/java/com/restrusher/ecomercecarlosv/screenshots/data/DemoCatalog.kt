package com.restrusher.ecomercecarlosv.screenshots.data

import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.screenshots.data.DemoClock.daysAgo
import com.restrusher.ecomercecarlosv.screenshots.data.DemoIds as Id

/**
 * Users, mercados and productos for the portfolio shots. Every `photoUrl` is null so Coil never
 * goes to the network; the screens draw their own placeholders instead.
 */
object DemoCatalog {

    val owner = AppUser(
        id = Id.OWNER,
        email = "carlos@pedidos.bo",
        name = "Carlos Vargas",
        role = UserRole.SUPERUSUARIO,
        phone = "+591 71234567",
        createdAt = daysAgo(400),
    )

    val umbrales = UmbralesEntity(montoMaximo = 500.0, diasMaximos = 30)

    val users = listOf(
        owner.toEntity(),
        user("user-lucia", "Lucía Mendoza", UserRole.USUARIO),
        user("user-diego", "Diego Rojas", UserRole.USUARIO),
        user("user-marta", "Marta Ríos", UserRole.INVITADO),
    )

    val mercados = listOf(
        mercado(Id.MERCADO_RODRIGUEZ, "Mercado Rodríguez", "Calle Rodríguez esq. Zoilo Flores"),
        mercado(Id.MERCADO_LANZA, "Mercado Lanza", "Av. Figueroa, Zona Central"),
        mercado(Id.MERCADO_CAMACHO, "Mercado Camacho", "Av. Camacho esq. Bueno"),
        mercado(Id.MERCADO_SOPOCACHI, "Mercado Sopocachi", "Calle Fernando Guachalla"),
    )

    val productos = listOf(
        producto(Id.PRODUCTO_ARROZ, "Arroz grano de oro 1 kg", 12.50),
        producto(Id.PRODUCTO_AZUCAR, "Azúcar blanca 1 kg", 9.00),
        producto(Id.PRODUCTO_ACEITE, "Aceite vegetal 900 ml", 18.00),
        producto(Id.PRODUCTO_HARINA, "Harina de trigo 1 kg", 8.50),
        producto(Id.PRODUCTO_FIDEO, "Fideo tallarín 400 g", 6.00),
        producto(Id.PRODUCTO_LECHE, "Leche evaporada 400 g", 7.50),
        producto(Id.PRODUCTO_CAFE, "Café molido 250 g", 32.00),
        producto(Id.PRODUCTO_HUEVOS, "Huevos maple x30", 30.00),
    )

    fun mercadoName(id: String): String = mercados.first { it.id == id }.name

    fun productoName(id: String): String = productos.first { it.id == id }.name

    fun price(productoId: String): Double = productos.first { it.id == productoId }.price

    private fun AppUser.toEntity() = UserEntity(
        id = id, email = email, name = name, role = role.name, phone = phone,
        photoUrl = null, isActive = true, createdAt = createdAt, lastSeenAt = DemoClock.now,
    )

    private fun user(id: String, name: String, role: UserRole) = UserEntity(
        id = id, email = "${id.removePrefix("user-")}@pedidos.bo", name = name, role = role.name,
        photoUrl = null, isActive = true, createdAt = daysAgo(200), lastSeenAt = daysAgo(1),
    )

    private fun mercado(id: String, name: String, address: String) = MercadoEntity(
        id = id, name = name, address = address, photoUrl = null, mapsUrl = null,
        latitude = null, longitude = null, createdAt = daysAgo(300),
    )

    private fun producto(id: String, name: String, price: Double) = ProductoEntity(
        id = id, name = name, description = null, price = price, photoUrl = null,
        isActive = true, createdAt = daysAgo(300),
    )
}
