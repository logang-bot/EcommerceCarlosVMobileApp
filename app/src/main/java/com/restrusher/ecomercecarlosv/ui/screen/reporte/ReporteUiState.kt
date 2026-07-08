package com.restrusher.ecomercecarlosv.ui.screen.reporte

import com.restrusher.ecomercecarlosv.domain.model.PedidoLineItem

enum class ReporteMode { DIARIO, POR_CLIENTE }

enum class DiarioPreset { HOY, AYER, SEMANA, PERSONALIZADO }

enum class ClientePreset { MES, TRIMESTRE, ANIO, PERSONALIZADO }

data class HistorialItem(
    val pedidoId: String,
    val title: String,
    val date: Long,
    val total: Double,
    val paid: Double,
    val pending: Double,
    val isSaldoExtra: Boolean,
    val subtitle: String = "",
    val lines: List<PedidoLineItem> = emptyList(),
)

data class ClienteOption(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
)

data class ReporteUiState(
    val mode: ReporteMode = ReporteMode.DIARIO,
    // Diario
    val diarioPreset: DiarioPreset = DiarioPreset.HOY,
    val diarioFromMs: Long = 0L,
    val diarioToMs: Long = 0L,
    val diarioFacturado: Double = 0.0,
    val diarioPagado: Double = 0.0,
    val diarioPendiente: Double = 0.0,
    val diarioPedidos: List<HistorialItem> = emptyList(),
    // Por cliente
    val clientePreset: ClientePreset = ClientePreset.MES,
    val clienteFromMs: Long = 0L,
    val clienteToMs: Long = 0L,
    val selectedClienteId: String? = null,
    val selectedClienteName: String = "",
    val selectedClientePhotoUrl: String? = null,
    val selectedMercadoName: String = "",
    val facturado: Double = 0.0,
    val pagado: Double = 0.0,
    val saldo: Double = 0.0,
    val historial: List<HistorialItem> = emptyList(),
    val saldoExtras: List<HistorialItem> = emptyList(),
    // Shared
    val customDiarioFrom: Long? = null,
    val customDiarioTo: Long? = null,
    val customClienteFrom: Long? = null,
    val customClienteTo: Long? = null,
    val allClientes: List<ClienteOption> = emptyList(),
    val isLoading: Boolean = true,
)
