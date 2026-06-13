package com.restrusher.ecomercecarlosv.ui.screen.reporte

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.ReporteClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReporteClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
    private val pedidoRepository: PedidoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<ReporteClienteRoute>().clienteId
    private val _mercadoName = MutableStateFlow("")

    val uiState = combine(
        clienteRepository.getByIdFlow(clienteId),
        pedidoRepository.getByClienteWithLines(clienteId),
        _mercadoName,
    ) { cliente, pedidos, mercadoName ->
        if (cliente == null) return@combine ReporteClienteUiState(isLoading = false)
        val balance = pedidos.filter {
            it.status == PedidoStatus.PARTIAL ||
                (it.status == PedidoStatus.PENDING && it.isSaldoExtra)
        }.sumOf { it.pending }
        val unpaid = pedidos.count { !it.isSaldoExtra && it.status != PedidoStatus.PAID }
        ReporteClienteUiState(
            clienteName = cliente.name,
            clienteDesc = cliente.description,
            clientePhone = cliente.phones.firstOrNull().orEmpty(),
            mercadoName = mercadoName,
            balance = balance,
            unpaidCount = unpaid,
            totalCount = pedidos.size,
            pedidos = pedidos,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReporteClienteUiState(),
    )

    init {
        viewModelScope.launch {
            clienteRepository.getByIdFlow(clienteId).first { it != null }?.let { cliente ->
                _mercadoName.value = mercadoRepository.getById(cliente.mercadoId)?.name.orEmpty()
            }
        }
    }
}
