package com.restrusher.ecomercecarlosv.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
object MercadosRoute

@Serializable
data class DetalleMercadoRoute(val mercadoId: String)

@Serializable
data class CreateMercadoRoute(val mercadoId: String? = null)

@Serializable
object PerfilRoute

@Serializable
object EditarPerfilRoute

@Serializable
object GestionUsuariosRoute

@Serializable
data class UsuarioDetalleRoute(val userId: String)

@Serializable
object CrearUsuarioRoute

@Serializable
object BusquedaRoute

@Serializable
data class ClientesRoute(val mercadoId: String)

@Serializable
data class DetalleClienteRoute(val clienteId: String)

@Serializable
data class CreateClienteRoute(val mercadoId: String, val clienteId: String? = null)

@Serializable
data class CreateProductoRoute(val productId: String? = null)

@Serializable
object ListaNegraRoute

@Serializable
data class AgregarListaNegraRoute(val clienteId: String)

@Serializable
data class CreacionPedidoRoute(
    val clienteId: String,
    val clienteName: String,
    val mercadoName: String,
)

@Serializable
data class DetallePedidoRoute(val pedidoId: String)

@Serializable
data class SaldoExtraRoute(val clienteId: String)
