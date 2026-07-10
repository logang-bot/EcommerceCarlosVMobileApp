package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagoDto(
    val id: String,
    @SerialName("pedido_id") val pedidoId: String,
    val amount: Double,
    @SerialName("paid_at") val paidAt: Long,
)
