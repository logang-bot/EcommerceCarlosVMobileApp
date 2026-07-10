package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.usecase.RegistrarPagoUseCase
import com.restrusher.ecomercecarlosv.presentation.screens.DetallePedidoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetallePedidoViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val clienteRepository: ClienteRepository,
    private val registrarPagoUseCase: RegistrarPagoUseCase,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pedidoId: String = savedStateHandle.toRoute<DetallePedidoRoute>().pedidoId
    private val _showPagoSheet = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)
    private val _clienteName = MutableStateFlow("")

    val uiState = combine(
        combine(
            pedidoRepository.getByIdFlow(pedidoId),
            pedidoRepository.getDetallesByPedidoFlow(pedidoId),
            pedidoRepository.getPagosByPedidoFlow(pedidoId),
        ) { pedido, detalles, pagos -> Triple(pedido, detalles, pagos) },
        combine(_showPagoSheet, _isSaving, _clienteName) { showSheet, saving, name -> Triple(showSheet, saving, name) },
        sessionManager.currentUser,
    ) { pedidoTriple, sheetTriple, user ->
        val (pedido, detalles, pagos) = pedidoTriple
        val (showSheet, saving, clienteName) = sheetTriple
        DetallePedidoUiState(
            pedido = pedido,
            detalles = detalles,
            pagos = pagos,
            clienteName = clienteName,
            isLoading = pedido == null,
            isSaving = saving,
            showPagoSheet = showSheet,
            canWrite = user?.role != UserRole.INVITADO,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetallePedidoUiState(),
    )

    init {
        viewModelScope.launch {
            pedidoRepository.getByIdFlow(pedidoId).first()?.let { pedido ->
                _clienteName.value = clienteRepository.getById(pedido.clienteId)?.name ?: ""
            }
        }
    }

    fun onShowPagoSheet() { _showPagoSheet.value = true }
    fun onDismissPagoSheet() { _showPagoSheet.value = false }

    fun onMarcarPagado() {
        val pedido = uiState.value.pedido ?: return
        _isSaving.value = true
        viewModelScope.launch {
            registrarPagoUseCase(pedido, pedido.pending)
            _isSaving.value = false
        }
    }

    fun onRegistrarPago(amount: Double) {
        val pedido = uiState.value.pedido ?: return
        if (amount <= 0) return
        _isSaving.value = true
        _showPagoSheet.value = false
        viewModelScope.launch {
            registrarPagoUseCase(pedido, amount)
            _isSaving.value = false
        }
    }
}
