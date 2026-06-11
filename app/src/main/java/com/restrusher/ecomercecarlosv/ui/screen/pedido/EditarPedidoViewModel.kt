package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPedidoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditarPedidoViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val clienteRepository: ClienteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pedidoId: String = savedStateHandle.toRoute<EditarPedidoRoute>().pedidoId

    private val _pedidoState = MutableStateFlow<com.restrusher.ecomercecarlosv.domain.model.Pedido?>(null)
    private val _clienteName = MutableStateFlow("")
    private val _editedDate = MutableStateFlow(0L)
    private val _lines = MutableStateFlow<List<EditLineState>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _isSaving = MutableStateFlow(false)
    private val _showDatePicker = MutableStateFlow(false)
    private val _showDeleteConfirm = MutableStateFlow(false)
    private val _showPaymentSheet = MutableStateFlow(false)
    private val _editingLineIndex = MutableStateFlow<Int?>(null)

    val uiState = combine(
        combine(_pedidoState, _clienteName, _editedDate, _lines) { pedido, clienteName, date, lines ->
            EditarPedidoUiState(pedido = pedido, clienteName = clienteName, editedDate = date, lines = lines)
        },
        _isLoading,
        _isSaving,
        _showDatePicker,
    ) { base, loading, saving, showDate ->
        base.copy(isLoading = loading, isSaving = saving, showDatePicker = showDate)
    }.combine(_showDeleteConfirm) { state, showDelete ->
        state.copy(showDeleteConfirm = showDelete)
    }.combine(_showPaymentSheet) { state, showSheet ->
        state.copy(showPaymentSheet = showSheet)
    }.combine(_editingLineIndex) { state, idx ->
        state.copy(editingLineIndex = idx)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditarPedidoUiState(),
    )

    init {
        viewModelScope.launch {
            val pedido = pedidoRepository.getByIdFlow(pedidoId).first()
            _pedidoState.value = pedido
            _editedDate.value = pedido?.createdAt ?: 0L
            _lines.value = pedidoRepository.getDetallesByPedido(pedidoId).map { d ->
                EditLineState(
                    detalleId = d.id,
                    productoId = d.productoId,
                    productName = d.productName,
                    quantity = d.quantity,
                    unitPrice = d.unitPrice,
                    catalogPrice = d.catalogPrice,
                    notes = d.notes,
                )
            }
            pedido?.let {
                _clienteName.value = clienteRepository.getById(it.clienteId)?.name ?: ""
            }
            _isLoading.value = false
        }
    }

    fun onShowDatePicker() { _showDatePicker.value = true }
    fun onDismissDatePicker() { _showDatePicker.value = false }
    fun onDateChanged(date: Long) {
        _editedDate.value = date
        _showDatePicker.value = false
    }

    fun onShowDeleteConfirm() { _showDeleteConfirm.value = true }
    fun onDismissDeleteConfirm() { _showDeleteConfirm.value = false }

    fun onDeletePedido(onSuccess: () -> Unit) {
        viewModelScope.launch {
            pedidoRepository.delete(pedidoId)
            onSuccess()
        }
    }

    fun onQuantityChange(index: Int, delta: Int) {
        _lines.update { lines ->
            lines.toMutableList().also { list ->
                val line = list[index]
                list[index] = line.copy(quantity = (line.quantity + delta).coerceAtLeast(1))
            }
        }
    }

    fun onRemoveLine(index: Int) {
        _lines.update { it.toMutableList().also { list -> list.removeAt(index) } }
    }

    fun onShowEditLine(index: Int) { _editingLineIndex.value = index }
    fun onDismissEditLine() { _editingLineIndex.value = null }

    fun onSaveLine(index: Int, unitPrice: Double, notes: String?) {
        _lines.update { lines ->
            lines.toMutableList().also { list ->
                list[index] = list[index].copy(unitPrice = unitPrice, notes = notes)
            }
        }
        _editingLineIndex.value = null
    }

    fun onShowPaymentSheet() { _showPaymentSheet.value = true }
    fun onDismissPaymentSheet() { _showPaymentSheet.value = false }

    fun onSave(payment: Double, onSuccess: () -> Unit) {
        val pedido = _pedidoState.value ?: return
        _isSaving.value = true
        _showPaymentSheet.value = false
        viewModelScope.launch {
            val currentLines = _lines.value
            val newTotal = currentLines.sumOf { it.subtotal }
            val detalles = currentLines.map { l ->
                DetallePedido(
                    id = l.detalleId,
                    pedidoId = pedidoId,
                    productoId = l.productoId,
                    productName = l.productName,
                    quantity = l.quantity,
                    unitPrice = l.unitPrice,
                    catalogPrice = l.catalogPrice,
                    notes = l.notes,
                )
            }
            val editedDate = _editedDate.value
            if (editedDate != pedido.createdAt) {
                pedidoRepository.updateDate(pedidoId, editedDate)
            }
            val paidAt = if (payment > 0) System.currentTimeMillis() else null
            pedidoRepository.updateLines(
                pedidoId = pedidoId,
                detalles = detalles,
                newTotal = newTotal,
                paid = payment,
                paidAt = paidAt,
            )
            _isSaving.value = false
            onSuccess()
        }
    }
}
