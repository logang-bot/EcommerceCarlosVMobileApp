package com.restrusher.ecomercecarlosv.domain.model

data class Pago(
    val id: String,
    val pedidoId: String,
    val amount: Double,
    val paidAt: Long,
)
