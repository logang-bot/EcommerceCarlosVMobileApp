package com.restrusher.ecomercecarlosv.ui.screen.reporte

import com.restrusher.ecomercecarlosv.domain.model.Pedido

enum class ReporteClientePreset { HOY, SEMANA, MES, PERSONALIZADO }

data class ReporteClienteUiState(
    val clienteName: String = "",
    val clienteDesc: String = "",
    val clientePhone: String = "",
    val mercadoName: String = "",
    val preset: ReporteClientePreset = ReporteClientePreset.HOY,
    val fromMs: Long = 0L,
    val toMs: Long = 0L,
    val customFrom: Long? = null,
    val customTo: Long? = null,
    val pedidosInRange: List<Pedido> = emptyList(),
    val pedidosCount: Int = 0,
    val montoTotal: Double = 0.0,
    val showWarning: Boolean = false,
    val isLoading: Boolean = true,
)
