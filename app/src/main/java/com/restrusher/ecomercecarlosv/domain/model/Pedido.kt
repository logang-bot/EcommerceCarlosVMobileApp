package com.restrusher.ecomercecarlosv.domain.model

data class Pedido(
    val id: String,
    val clienteId: String,
    val status: PedidoStatus,
    val total: Double,
    val paid: Double,
    val notes: String? = null,
    val createdAt: Long,
    val paidAt: Long? = null,
    val isSaldoExtra: Boolean = false,
    val itemCount: Int = 0,
    val lines: List<PedidoLineItem> = emptyList(),
) {
    val pending: Double get() = (total - paid).coerceAtLeast(0.0)
}
