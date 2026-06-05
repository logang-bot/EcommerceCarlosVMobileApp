package com.restrusher.ecomercecarlosv.ui.screen.cliente

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus

data class DetalleClienteUiState(
    val cliente: Cliente? = null,
    val status: ClientStatus = ClientStatus.AL_DIA,
    val balance: Double = 0.0,
    val isLoading: Boolean = true,
)
