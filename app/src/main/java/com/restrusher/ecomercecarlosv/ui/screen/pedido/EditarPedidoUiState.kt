package com.restrusher.ecomercecarlosv.ui.screen.pedido

import com.restrusher.ecomercecarlosv.domain.model.Pedido

data class EditLineState(
    val detalleId: String,
    val productoId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val catalogPrice: Double,
    val notes: String?,
) {
    val subtotal: Double get() = unitPrice * quantity
    val isPriceModified: Boolean get() = unitPrice != catalogPrice
}

data class EditarPedidoUiState(
    val pedido: Pedido? = null,
    val clienteName: String = "",
    val editedDate: Long = 0L,
    val lines: List<EditLineState> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDatePicker: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val editingLineIndex: Int? = null,
) {
    val newTotal: Double get() = lines.sumOf { it.subtotal }
}
