package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.data.prefs.UmbralesManager
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
    umbralesManager: UmbralesManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<DetalleClienteRoute>().clienteId

    private val _showUnblacklistSheet = MutableStateFlow(false)

    val uiState: StateFlow<DetalleClienteUiState> = combine(
        clienteRepository.getByIdFlow(clienteId),
        pedidoRepository.getByCliente(clienteId),
        umbralesManager.umbrales,
        _showUnblacklistSheet,
    ) { cliente, pedidos, umbrales, showSheet ->
        val balance = pedidos.filter {
            it.status == PedidoStatus.PARTIAL ||
                (it.status == PedidoStatus.PENDING && it.isSaldoExtra)
        }.sumOf { it.pending }
        DetalleClienteUiState(
            cliente = cliente,
            pedidos = pedidos,
            balance = balance,
            status = computeStatus(balance, pedidos, umbrales),
            isLoading = false,
            showUnblacklistSheet = showSheet,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DetalleClienteUiState(),
    )

    fun onQuitarListaNegraClick() {
        val cliente = uiState.value.cliente ?: return
        if (cliente.blacklistIsManualAmount) {
            _showUnblacklistSheet.value = true
        } else {
            viewModelScope.launch { clienteRepository.unblacklist(clienteId) }
        }
    }

    fun dismissUnblacklistSheet() {
        _showUnblacklistSheet.value = false
    }

    fun unblacklistRestore() {
        _showUnblacklistSheet.value = false
        viewModelScope.launch { clienteRepository.unblacklist(clienteId) }
    }

    fun unblacklistMarkAllPaid() {
        _showUnblacklistSheet.value = false
        viewModelScope.launch {
            pedidoRepository.markAllPaidForCliente(clienteId)
            clienteRepository.unblacklist(clienteId)
        }
    }

    private fun computeStatus(balance: Double, pedidos: List<Pedido>, umbrales: Umbrales): ClientStatus {
        if (balance <= 0.0) return ClientStatus.AL_DIA
        val hasOldUnpaid = pedidos.any {
            (it.status == PedidoStatus.PARTIAL || (it.status == PedidoStatus.PENDING && it.isSaldoExtra)) &&
                isOlderThan(it.createdAt, umbrales.diasMaximos)
        }
        return if (hasOldUnpaid || balance > umbrales.montoMaximo) ClientStatus.CRITICO else ClientStatus.ADVERTENCIA
    }

    private fun isOlderThan(createdAt: Long, days: Int): Boolean =
        (System.currentTimeMillis() - createdAt) > days.toLong() * 24 * 60 * 60 * 1000
}
