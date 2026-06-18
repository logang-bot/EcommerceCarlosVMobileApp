package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

data class BlacklistUiModel(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val blacklistBalance: Double,
    val blacklistReason: String?,
    val blacklistedAt: Long?,
)

data class ListaNegraUiState(
    val items: List<BlacklistUiModel> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val refreshFailed: Boolean = false,
) {
    val filtered: List<BlacklistUiModel>
        get() = if (query.isBlank()) items
        else items.filter { it.name.contains(query, ignoreCase = true) }

    val totalBalance: Double get() = items.sumOf { it.blacklistBalance }
}
