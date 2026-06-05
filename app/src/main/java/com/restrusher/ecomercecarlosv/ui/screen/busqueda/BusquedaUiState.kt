package com.restrusher.ecomercecarlosv.ui.screen.busqueda

import com.restrusher.ecomercecarlosv.domain.model.ClientStatus

data class ClienteSearchResult(
    val clienteId: String,
    val name: String,
    val mercadoName: String,
    val status: ClientStatus,
    val balance: Double,
)

data class BusquedaUiState(
    val query: String = "",
    val results: List<ClienteSearchResult> = emptyList(),
    val isSearching: Boolean = false,
)
