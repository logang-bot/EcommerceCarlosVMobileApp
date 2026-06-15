package com.restrusher.ecomercecarlosv.ui.screen.cliente

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus

enum class ClienteSortMode { AZ, CRITICOS_FIRST, MAYOR_SALDO, SOLO_CON_DEUDA }

data class ClienteUiModel(
    val cliente: Cliente,
    val status: ClientStatus,
    val balance: Double,
)

data class ClientesUiState(
    val clientes: List<ClienteUiModel> = emptyList(),
    val mercadoId: String = "",
    val mercadoName: String = "",
    val sortMode: ClienteSortMode = ClienteSortMode.AZ,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val canWrite: Boolean = true,
)
