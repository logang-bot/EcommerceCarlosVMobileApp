package com.restrusher.ecomercecarlosv.ui.screen.pedido

data class CartItem(
    val productoId: String,
    val productName: String,
    val unitPrice: Double,
    val catalogPrice: Double,
    val quantity: Int = 1,
    val notes: String? = null,
) {
    val subtotal: Double get() = unitPrice * quantity
    val isPriceModified: Boolean get() = unitPrice != catalogPrice
}
