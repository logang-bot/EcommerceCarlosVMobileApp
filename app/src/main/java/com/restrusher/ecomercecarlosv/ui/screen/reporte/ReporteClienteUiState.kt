package com.restrusher.ecomercecarlosv.ui.screen.reporte

import com.restrusher.ecomercecarlosv.domain.model.Pedido

data class ReporteClienteUiState(
    val clienteName: String = "",
    val clienteDesc: String = "",
    val clientePhone: String = "",
    val mercadoName: String = "",
    val balance: Double = 0.0,
    val unpaidCount: Int = 0,
    val totalCount: Int = 0,
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = true,
)
