package com.restrusher.ecomercecarlosv.ui.screen.mercado

import com.restrusher.ecomercecarlosv.domain.model.Mercado

data class DetalleMercadoUiState(
    val mercado: Mercado? = null,
    val mercadoId: String = "",
    val isLoading: Boolean = true,
    val canWrite: Boolean = true,
    val showDeleteDialog: Boolean = false,
    /** Live clientes that would go with the mercado — shown in the delete confirmation. */
    val clienteCount: Int = 0,
)
