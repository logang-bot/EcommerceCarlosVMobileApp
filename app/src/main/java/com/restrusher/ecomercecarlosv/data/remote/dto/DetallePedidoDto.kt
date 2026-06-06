package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetallePedidoDto(
    val id: String,
    @SerialName("pedido_id") val pedidoId: String,
    @SerialName("producto_id") val productoId: String,
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("catalog_price") val catalogPrice: Double,
    val notes: String?,
)
