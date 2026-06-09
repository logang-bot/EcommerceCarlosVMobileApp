package com.restrusher.ecomercecarlosv.ui.screen.pedido

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido

data class DetallePedidoUiState(
    val pedido: Pedido? = null,
    val detalles: List<DetallePedido> = emptyList(),
    val clienteName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showPagoSheet: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showDatePicker: Boolean = false,
)
