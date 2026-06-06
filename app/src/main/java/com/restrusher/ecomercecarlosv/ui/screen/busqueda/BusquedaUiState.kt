package com.restrusher.ecomercecarlosv.ui.screen.busqueda

import com.restrusher.ecomercecarlosv.domain.model.ClientStatus

data class ClienteSearchResult(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val status: ClientStatus,
    val balance: Double,
)

data class MercadoSearchResult(
    val mercadoId: String,
    val name: String,
    val photoUrl: String?,
    val clientesCount: Int,
)

data class BusquedaUiState(
    val query: String = "",
    val clienteResults: List<ClienteSearchResult> = emptyList(),
    val mercadoResults: List<MercadoSearchResult> = emptyList(),
    val isSearching: Boolean = false,
) {
    val hasResults get() = clienteResults.isNotEmpty() || mercadoResults.isNotEmpty()
}
