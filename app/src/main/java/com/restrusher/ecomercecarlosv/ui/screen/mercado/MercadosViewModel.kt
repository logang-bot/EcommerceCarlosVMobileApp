package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MercadosViewModel @Inject constructor(
    mercadoRepository: MercadoRepository,
    clienteRepository: ClienteRepository,
    pedidoRepository: PedidoRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _selectedMercadoId = MutableStateFlow<String?>(null)

    val uiState = combine(
        combine(
            mercadoRepository.getAll(),
            clienteRepository.getAll(),
            pedidoRepository.getAllUnpaid(),
        ) { mercados, clientes, unpaid -> Triple(mercados, clientes, unpaid) },
        sessionManager.currentUser,
        _selectedMercadoId,
    ) { triple, user, selectedId ->
        val (mercados, clientes, unpaidPedidos) = triple
        val initials = user?.name
            ?.split(' ')
            ?.filter(String::isNotBlank)
            ?.take(2)
            ?.map { it.first().uppercaseChar() }
            ?.joinToString("") ?: ""
        MercadosUiState(
            mercados = mercados,
            stats = buildStats(mercados, clientes, unpaidPedidos),
            isLoading = false,
            currentUserInitials = initials,
            selectedMercadoId = selectedId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MercadosUiState(isLoading = true),
    )

    fun onMercadoLongPress(mercadoId: String) {
        _selectedMercadoId.value = if (_selectedMercadoId.value == mercadoId) null else mercadoId
    }

    fun clearSelection() {
        _selectedMercadoId.value = null
    }

    private fun buildStats(
        mercados: List<Mercado>,
        clientes: List<Cliente>,
        unpaidPedidos: List<Pedido>,
    ): Map<String, MercadoStat> {
        val clientesByMercado = clientes.groupBy { it.mercadoId }
        val pedidosByCliente = unpaidPedidos.groupBy { it.clienteId }
        return mercados.associate { mercado ->
            val mercadoClientes = clientesByMercado[mercado.id].orEmpty()
            var hasWarning = false
            var hasCritical = false
            for (cliente in mercadoClientes) {
                val pedidos = pedidosByCliente[cliente.id].orEmpty()
                val balance = pedidos.sumOf { it.pending }
                if (balance > 0) {
                    if (pedidos.any { isOlderThan30Days(it.createdAt) } || balance > 200.0) {
                        hasCritical = true
                    } else {
                        hasWarning = true
                    }
                }
            }
            mercado.id to MercadoStat(
                activeClientCount = mercadoClientes.size,
                hasWarning = hasWarning,
                hasCritical = hasCritical,
            )
        }
    }

    private fun isOlderThan30Days(createdAt: Long): Boolean =
        (System.currentTimeMillis() - createdAt) > 30L * 24 * 60 * 60 * 1000
}
