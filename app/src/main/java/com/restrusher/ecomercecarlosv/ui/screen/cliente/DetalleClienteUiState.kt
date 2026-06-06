package com.restrusher.ecomercecarlosv.ui.screen.cliente

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido

data class DetalleClienteUiState(
    val cliente: Cliente? = null,
    val status: ClientStatus = ClientStatus.AL_DIA,
    val balance: Double = 0.0,
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = true,
)
