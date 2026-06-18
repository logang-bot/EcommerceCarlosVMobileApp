package com.restrusher.ecomercecarlosv.ui.screen.producto

import com.restrusher.ecomercecarlosv.domain.model.Producto

data class CatalogoUiState(
    val productos: List<Producto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val canWrite: Boolean = true,
    val isRefreshing: Boolean = false,
    val refreshFailed: Boolean = false,
)
