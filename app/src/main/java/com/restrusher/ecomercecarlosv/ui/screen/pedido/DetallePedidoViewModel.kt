package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetallePedidoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetallePedidoViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pedidoId: String = savedStateHandle.toRoute<DetallePedidoRoute>().pedidoId
    private val _detalles = MutableStateFlow<List<DetallePedido>>(emptyList())
    private val _showPagoSheet = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)

    val uiState = combine(
        pedidoRepository.getByIdFlow(pedidoId),
        _detalles,
        _showPagoSheet,
        _isSaving,
    ) { pedido, detalles, showSheet, saving ->
        DetallePedidoUiState(
            pedido = pedido,
            detalles = detalles,
            isLoading = pedido == null,
            isSaving = saving,
            showPagoSheet = showSheet,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetallePedidoUiState(),
    )

    init {
        viewModelScope.launch {
            _detalles.value = pedidoRepository.getDetallesByPedido(pedidoId)
        }
    }

    fun onShowPagoSheet() { _showPagoSheet.value = true }
    fun onDismissPagoSheet() { _showPagoSheet.value = false }

    fun onMarcarPagado() {
        val pedido = uiState.value.pedido ?: return
        _isSaving.value = true
        viewModelScope.launch {
            pedidoRepository.updateStatus(
                id = pedidoId,
                status = PedidoStatus.PAID,
                paid = pedido.total,
                paidAt = System.currentTimeMillis(),
            )
            _isSaving.value = false
        }
    }

    fun onRegistrarPago(amount: Double) {
        val pedido = uiState.value.pedido ?: return
        if (amount <= 0) return
        _isSaving.value = true
        _showPagoSheet.value = false
        viewModelScope.launch {
            val newPaid = (pedido.paid + amount).coerceAtMost(pedido.total)
            val newStatus = if (newPaid >= pedido.total) PedidoStatus.PAID else PedidoStatus.PARTIAL
            pedidoRepository.updateStatus(
                id = pedidoId,
                status = newStatus,
                paid = newPaid,
                paidAt = if (newStatus == PedidoStatus.PAID) System.currentTimeMillis() else null,
            )
            _isSaving.value = false
        }
    }
}
