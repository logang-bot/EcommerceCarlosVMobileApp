package com.restrusher.ecomercecarlosv.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object MercadosRoute

@Serializable
data class DetalleMercadoRoute(val mercadoId: String)

@Serializable
data class CreateMercadoRoute(val mercadoId: String? = null)
