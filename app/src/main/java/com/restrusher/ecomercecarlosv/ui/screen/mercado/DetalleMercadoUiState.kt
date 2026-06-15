package com.restrusher.ecomercecarlosv.ui.screen.mercado

import com.restrusher.ecomercecarlosv.domain.model.Mercado

data class DetalleMercadoUiState(
    val mercado: Mercado? = null,
    val mercadoId: String = "",
    val isLoading: Boolean = true,
    val canWrite: Boolean = true,
)
