package com.restrusher.ecomercecarlosv.ui.screen.cliente

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus

data class DetalleClienteUiState(
    val cliente: Cliente? = null,
    val status: ClientStatus = ClientStatus.AL_DIA,
    val balance: Double = 0.0,
    val pedidosBalance: Double = 0.0,
    val unpaidPedidosCount: Int = 0,
    val extraBalance: Double = 0.0,
    val unpaidExtraCount: Int = 0,
    val pedidos: List<Pedido> = emptyList(),
    val allPedidosCount: Int = 0,
    val pedidoFilters: Set<PedidoStatus> = emptySet(),
    val isLoading: Boolean = true,
    val showUnblacklistSheet: Boolean = false,
    val canWrite: Boolean = true,
)
