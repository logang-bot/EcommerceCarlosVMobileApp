package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

data class AgregarListaNegraUiState(
    val clienteId: String = "",
    val clienteName: String = "",
    val manualAmount: String = "",
    val reason: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val canConfirm get() = reason.isNotBlank() && (manualAmount.toDoubleOrNull() ?: 0.0) > 0.0
}
