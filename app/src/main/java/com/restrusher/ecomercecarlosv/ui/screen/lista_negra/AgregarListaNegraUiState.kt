package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import com.restrusher.ecomercecarlosv.domain.model.Pedido

enum class TotalMode { AUTO, MANUAL }

data class AgregarListaNegraUiState(
    val clienteId: String = "",
    val clienteName: String = "",
    val pendingPedidos: List<Pedido> = emptyList(),
    val totalMode: TotalMode = TotalMode.MANUAL,
    val manualAmount: String = "",
    val reason: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val autoAmount: Double get() = pendingPedidos.sumOf { it.pending }
    val effectiveAmount: Double get() = when (totalMode) {
        TotalMode.AUTO -> autoAmount
        TotalMode.MANUAL -> manualAmount.toDoubleOrNull() ?: 0.0
    }
    val canConfirm: Boolean get() = reason.isNotBlank() && effectiveAmount > 0.0
}
