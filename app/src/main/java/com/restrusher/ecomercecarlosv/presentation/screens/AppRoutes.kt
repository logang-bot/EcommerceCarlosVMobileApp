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
