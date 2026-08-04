package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.RefreshClienteDataUseCase
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
    private val umbralesRepository: UmbralesRepository,
    private val sessionManager: SessionManager,
    private val refreshClienteData: RefreshClienteDataUseCase,
    private val calcularEstadoCliente: CalcularEstadoClienteUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val mercadoId: String = savedStateHandle.toRoute<ClientesRoute>().mercadoId

    private val _sortMode = MutableStateFlow(ClienteSortMode.AZ)
    private val _mercadoName = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshFailed = MutableStateFlow(false)

    val uiState = combine(
        combine(
            clienteRepository.getByMercado(mercadoId),
            pedidoRepository.getAllUnpaid(),
            umbralesRepository.getUmbrales(),
            clienteRepository.isSyncing,
        ) { clientes, allUnpaid, umbrales, isSyncing ->
            val pedidosByCliente = allUnpaid.groupBy { it.clienteId }
            val models = clientes.map { cliente ->
                val pedidos = pedidosByCliente[cliente.id].orEmpty()
                val balance = pedidos.filter {
                    it.status == PedidoStatus.PARTIAL ||
                        (it.status == PedidoStatus.PENDING && it.isSaldoExtra)
                }.sumOf { it.pending }
                ClienteUiModel(cliente, calcularEstadoCliente(pedidos, umbrales), balance)
            }
            Pair(models, isSyncing && clientes.isEmpty())
        },
        combine(_sortMode, _mercadoName, _searchQuery) { sort, name, query -> Triple(sort, name, query) },
        sessionManager.currentUser,
    ) { (models, isLoading), sortTriple, user ->
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
            isLoading = isLoading,
            canWrite = user?.role != UserRole.INVITADO,
        )
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_refreshFailed) { state, failed ->
        state.copy(refreshFailed = failed)
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

    fun onSortChange(mode: ClienteSortMode) {
        _sortMode.value = mode
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onRefresh() {
        _refreshFailed.value = false
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshFailed.value = !refreshClienteData()
            _isRefreshing.value = false
        }
    }

    fun onRefreshErrorDismissed() { _refreshFailed.value = false }
}
