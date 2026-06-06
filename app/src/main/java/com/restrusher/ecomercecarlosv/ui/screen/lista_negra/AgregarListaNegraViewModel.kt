package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.AgregarListaNegraRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgregarListaNegraViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<AgregarListaNegraRoute>().clienteId

    private val _state = MutableStateFlow(AgregarListaNegraUiState())
    val uiState: StateFlow<AgregarListaNegraUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                clienteRepository.getByIdFlow(clienteId),
                pedidoRepository.getByCliente(clienteId),
            ) { cliente, pedidos ->
                val pending = pedidos.filter { it.status != PedidoStatus.PAID }
                val current = _state.value
                // Default to AUTO on first load when there are pending pedidos
                val mode = if (current.isLoading) {
                    if (pending.isNotEmpty()) TotalMode.AUTO else TotalMode.MANUAL
                } else {
                    current.totalMode
                }
                current.copy(
                    clienteId = clienteId,
                    clienteName = cliente?.name ?: "",
                    pendingPedidos = pending,
                    totalMode = mode,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    fun onAmountChange(v: String) { _state.value = _state.value.copy(manualAmount = v) }
    fun onReasonChange(v: String) { _state.value = _state.value.copy(reason = v) }
    fun onTotalModeChange(mode: TotalMode) { _state.value = _state.value.copy(totalMode = mode) }

    fun onConfirm(onSuccess: () -> Unit) {
        val s = _state.value
        if (!s.canConfirm) return
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            clienteRepository.blacklist(
                id = clienteId,
                reason = s.reason,
                balance = s.effectiveAmount,
                at = System.currentTimeMillis(),
            )
            onSuccess()
        }
    }
}
