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
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
    private val pedidoRepository: PedidoRepository,
    private val umbralesManager: UmbralesManager,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val mercadoId: String = savedStateHandle.toRoute<ClientesRoute>().mercadoId

    private val _sortMode = MutableStateFlow(ClienteSortMode.AZ)
    private val _mercadoName = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")

    val uiState = combine(
        combine(
            clienteRepository.getByMercado(mercadoId),
            pedidoRepository.getAllUnpaid(),
            umbralesManager.umbrales,
        ) { clientes, allUnpaid, umbrales ->
            val pedidosByCliente = allUnpaid.groupBy { it.clienteId }
            clientes.map { cliente ->
                val pedidos = pedidosByCliente[cliente.id].orEmpty()
                val balance = pedidos.filter {
                    it.status == PedidoStatus.PARTIAL ||
                        (it.status == PedidoStatus.PENDING && it.isSaldoExtra)
                }.sumOf { it.pending }
                val statusBalance = pedidos.filter {
                    it.status == PedidoStatus.PARTIAL && !it.isSaldoExtra
                }.sumOf { it.pending }
                ClienteUiModel(cliente, computeStatus(statusBalance, pedidos, umbrales), balance)
            }
        },
        combine(_sortMode, _mercadoName, _searchQuery) { sort, name, query -> Triple(sort, name, query) },
        sessionManager.currentUser,
    ) { models, sortTriple, user ->
        val (sort, name, query) = sortTriple
        val filtered = if (query.isBlank()) models
        else models.filter { it.cliente.name.contains(query, ignoreCase = true) }
        val sorted = when (sort) {
            ClienteSortMode.AZ -> filtered.sortedBy { it.cliente.name }
            ClienteSortMode.CRITICOS_FIRST -> filtered.sortedWith(
                compareBy<ClienteUiModel> { it.status.ordinal }.thenByDescending { it.balance },
            )
            ClienteSortMode.MAYOR_SALDO -> filtered.sortedByDescending { it.balance }
            ClienteSortMode.SOLO_CON_DEUDA -> filtered.filter { it.balance > 0 }.sortedByDescending { it.balance }
        }
        ClientesUiState(
            clientes = sorted,
            mercadoId = mercadoId,
            mercadoName = name,
            sortMode = sort,
            searchQuery = query,
            isLoading = false,
            canWrite = user?.role != UserRole.INVITADO,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientesUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            _mercadoName.value = mercadoRepository.getById(mercadoId)?.name.orEmpty()
        }
    }

    fun onSortChange(mode: ClienteSortMode) { _sortMode.value = mode }
    fun onSearchChange(query: String) { _searchQuery.value = query }

    private fun computeStatus(statusBalance: Double, pedidos: List<Pedido>, umbrales: Umbrales): ClientStatus {
        if (statusBalance <= 0.0) return ClientStatus.AL_DIA
        val hasOldUnpaid = pedidos.any {
            it.status == PedidoStatus.PARTIAL && !it.isSaldoExtra &&
                isOlderThan(it.createdAt, umbrales.diasMaximos)
        }
        return if (hasOldUnpaid || statusBalance > umbrales.montoMaximo) ClientStatus.CRITICO else ClientStatus.ADVERTENCIA
    }

    private fun isOlderThan(createdAt: Long, days: Int): Boolean =
        (System.currentTimeMillis() - createdAt) > days.toLong() * 24 * 60 * 60 * 1000
}
