package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.CreateSaldoExtraUseCase
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
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
    private val createSaldoExtraUseCase: CreateSaldoExtraUseCase,
    private val calcularEstadoCliente: CalcularEstadoClienteUseCase,
    umbralesRepository: UmbralesRepository,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<DetalleClienteRoute>().clienteId

    private val _showUnblacklistSheet = MutableStateFlow(false)
    private val _pedidoFilters = MutableStateFlow<Set<PedidoStatus>>(emptySet())

    val uiState: StateFlow<DetalleClienteUiState> = combine(
        combine(
            clienteRepository.getByIdFlow(clienteId),
            pedidoRepository.getByClienteWithLines(clienteId),
            umbralesRepository.getUmbrales(),
            pedidoRepository.isSyncing,
        ) { cliente, pedidos, umbrales, isSyncing ->
            Pair(Triple(cliente, pedidos, umbrales), isSyncing && pedidos.isEmpty())
        },
        combine(_showUnblacklistSheet, _pedidoFilters, sessionManager.currentUser) { showSheet, filters, user ->
            Triple(showSheet, filters, user)
        },
    ) { (triple1, isLoading), triple2 ->
        val (cliente, pedidos, umbrales) = triple1
        val (showSheet, filters, user) = triple2
        val unpaidRegular = pedidos.filter { !it.isSaldoExtra && it.status != PedidoStatus.PAID }
        val unpaidExtra = pedidos.filter { it.isSaldoExtra && it.status != PedidoStatus.PAID }
        val pedidosBalance = unpaidRegular.sumOf { it.pending }
        val extraBalance = unpaidExtra.sumOf { it.pending }
        val filteredPedidos = if (filters.isEmpty()) pedidos else pedidos.filter { it.status in filters }

        DetalleClienteUiState(
            cliente = cliente,
            pedidos = filteredPedidos,
            allPedidosCount = pedidos.size,
            balance = pedidosBalance + extraBalance,
            pedidosBalance = pedidosBalance,
            unpaidPedidosCount = unpaidRegular.size,
            extraBalance = extraBalance,
            unpaidExtraCount = unpaidExtra.size,
            status = calcularEstadoCliente(pedidos, umbrales),
            isLoading = isLoading,
            showUnblacklistSheet = showSheet,
            pedidoFilters = filters,
            canWrite = user?.role != UserRole.INVITADO,
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
            val state = uiState.value
            val blacklistBalance = state.cliente?.blacklistBalance ?: 0.0
            val pendingSum = state.pedidosBalance + state.extraBalance

            pedidoRepository.markAllPaidForCliente(clienteId)

            val excess = blacklistBalance - pendingSum
            if (excess > 0.01) {
                createSaldoExtraUseCase(
                    clienteId = clienteId,
                    description = SALDO_EXTRA_DESC,
                    amount = excess,
                    date = System.currentTimeMillis(),
                )
            }

            clienteRepository.unblacklist(clienteId)
        }
    }

    fun onTogglePedidoFilter(status: PedidoStatus) {
        _pedidoFilters.value = _pedidoFilters.value.toMutableSet().apply {
            if (contains(status)) remove(status) else add(status)
        }
    }

    fun onClearPedidoFilters() {
        _pedidoFilters.value = emptySet()
    }

    companion object {
        private const val SALDO_EXTRA_DESC = "Saldo de liquidación al reactivar"
    }
}
