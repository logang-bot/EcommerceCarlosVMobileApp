package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
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
    private val umbralesRepository: UmbralesRepository,
    private val calcularEstadoCliente: CalcularEstadoClienteUseCase,
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
            umbralesRepository.getUmbrales(),
        ) { mercados, clientes, unpaid, umbrales -> MercadoInputs(mercados, clientes, unpaid, umbrales) },
        sessionManager.currentUser,
        _selectedMercadoId,
        mercadoRepository.isSyncing,
        syncOperationDao.observeAll(),
    ) { inputs, user, selectedId, isSyncing, allOps ->
        val (mercados, clientes, unpaidPedidos, umbrales) = inputs
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
            stats = buildStats(mercados, clientes, unpaidPedidos, umbrales),
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

    /**
     * The dot means exactly what the cliente's own badge means — [CalcularEstadoClienteUseCase] is
     * the single rule. `now` is read once per emission rather than per cliente so every client in
     * one refresh is judged against the same instant.
     */
    private fun buildStats(
        mercados: List<Mercado>,
        clientes: List<Cliente>,
        unpaidPedidos: List<Pedido>,
        umbrales: Umbrales,
        now: Long = System.currentTimeMillis(),
    ): Map<String, MercadoStat> {
        val clientesByMercado = clientes.groupBy { it.mercadoId }
        val pedidosByCliente = unpaidPedidos.groupBy { it.clienteId }
        return mercados.associate { mercado ->
            val mercadoClientes = clientesByMercado[mercado.id].orEmpty()
            var hasWarning = false
            var hasCritical = false
            for (cliente in mercadoClientes) {
                val pedidos = pedidosByCliente[cliente.id].orEmpty()
                when (calcularEstadoCliente(pedidos, umbrales, now)) {
                    ClientStatus.CRITICO -> hasCritical = true
                    ClientStatus.ADVERTENCIA -> hasWarning = true
                    ClientStatus.AL_DIA -> Unit
                }
            }
            mercado.id to MercadoStat(
                activeClientCount = mercadoClientes.size,
                hasWarning = hasWarning,
                hasCritical = hasCritical,
            )
        }
    }

    private data class MercadoInputs(
        val mercados: List<Mercado>,
        val clientes: List<Cliente>,
        val unpaidPedidos: List<Pedido>,
        val umbrales: Umbrales,
    )
}
