package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    pedidoRepository: PedidoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<DetalleClienteRoute>().clienteId

    val uiState: StateFlow<DetalleClienteUiState> = combine(
        clienteRepository.getByIdFlow(clienteId),
        pedidoRepository.getByCliente(clienteId),
    ) { cliente, pedidos ->
        val balance = pedidos.filter { it.status != PedidoStatus.PAID }.sumOf { it.pending }
        DetalleClienteUiState(
            cliente = cliente,
            pedidos = pedidos,
            balance = balance,
            status = computeStatus(balance, pedidos),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleClienteUiState(),
    )

    private fun computeStatus(balance: Double, pedidos: List<Pedido>): ClientStatus {
        if (balance <= 0.0) return ClientStatus.AL_DIA
        val hasOldUnpaid = pedidos.any { it.status != PedidoStatus.PAID && isOlderThan30Days(it.createdAt) }
        return if (hasOldUnpaid || balance > 200.0) ClientStatus.CRITICO else ClientStatus.ADVERTENCIA
    }

    fun unblacklist() {
        viewModelScope.launch { clienteRepository.unblacklist(clienteId) }
    }

    private fun isOlderThan30Days(createdAt: Long): Boolean =
        (System.currentTimeMillis() - createdAt) > 30L * 24 * 60 * 60 * 1000
}
