package com.restrusher.ecomercecarlosv.ui.screen.mercado

import com.restrusher.ecomercecarlosv.domain.model.Mercado

data class MercadosUiState(
    val mercados: List<Mercado> = emptyList(),
    val isLoading: Boolean = false,
)
