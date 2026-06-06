package com.restrusher.ecomercecarlosv.domain.model

data class DetallePedido(
    val id: String,
    val pedidoId: String,
    val productoId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val catalogPrice: Double,
    val notes: String? = null,
) {
    val subtotal: Double get() = unitPrice * quantity
    val isPriceModified: Boolean get() = unitPrice != catalogPrice
}
