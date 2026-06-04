package com.restrusher.ecomercecarlosv.ui.screen.mercado

import com.restrusher.ecomercecarlosv.domain.model.Mercado

data class DetalleMercadoUiState(
    val mercado: Mercado? = null,
    val isLoading: Boolean = true,
)
