package com.restrusher.ecomercecarlosv.ui.screen.pedido

import com.restrusher.ecomercecarlosv.domain.model.Producto

data class CreacionPedidoUiState(
    val clienteId: String = "",
    val clienteName: String = "",
    val mercadoName: String = "",
    val productos: List<Producto> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val showPaymentSheet: Boolean = false,
    val editingItem: CartItem? = null,
    val isSaving: Boolean = false,
    val savedPedidoId: String? = null,
) {
    val filteredProductos: List<Producto>
        get() = if (searchQuery.isBlank()) productos
                else productos.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val cartTotal: Double get() = cart.sumOf { it.subtotal }

    fun cartQuantityFor(productoId: String): Int =
        cart.find { it.productoId == productoId }?.quantity ?: 0
}
