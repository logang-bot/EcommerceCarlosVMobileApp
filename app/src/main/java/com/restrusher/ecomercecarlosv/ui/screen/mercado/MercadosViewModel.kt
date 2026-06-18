package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.usecase.RefreshMercadoDataUseCase
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MercadosViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
    sessionManager: SessionManager,
    syncOperationDao: SyncOperationDao,
    private val refreshMercadoData: RefreshMercadoDataUseCase,
) : ViewModel() {

    private val _selectedMercadoId = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshFailed = MutableStateFlow(false)

    val uiState = combine(
        combine(
            mercadoRepository.getAll(),
            clienteRepository.getAll(),
            pedidoRepository.getAllUnpaid(),
        ) { mercados, clientes, unpaid -> Triple(mercados, clientes, unpaid) },
        sessionManager.currentUser,
        _selectedMercadoId,
        mercadoRepository.isSyncing,
        syncOperationDao.observeAll(),
    ) { triple, user, selectedId, isSyncing, allOps ->
        val (mercados, clientes, unpaidPedidos) = triple
        val initials = user?.name
            ?.split(' ')
            ?.filter(String::isNotBlank)
            ?.take(2)
            ?.map { it.first().uppercaseChar() }
            ?.joinToString("") ?: ""
        val iconState = when {
            allOps.isEmpty() -> SyncIconState.SYNCED
            allOps.any { it.retryCount > 0 } -> SyncIconState.ERROR
            else -> SyncIconState.PENDING
        }
        MercadosUiState(
            mercados = mercados,
            stats = buildStats(mercados, clientes, unpaidPedidos),
            isLoading = isSyncing && mercados.isEmpty(),
            currentUserInitials = initials,
            currentUserPhotoUrl = user?.photoUrl,
            selectedMercadoId = selectedId,
            canWrite = user?.role != UserRole.INVITADO,
            syncIconState = iconState,
        )
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_refreshFailed) { state, failed ->
        state.copy(refreshFailed = failed)
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

    fun onRefresh() {
        _refreshFailed.value = false
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshFailed.value = !refreshMercadoData()
            _isRefreshing.value = false
        }
    }

    fun onRefreshErrorDismissed() { _refreshFailed.value = false }

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
