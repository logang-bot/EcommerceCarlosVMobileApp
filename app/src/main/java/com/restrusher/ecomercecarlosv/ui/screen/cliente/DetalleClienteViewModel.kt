package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<DetalleClienteRoute>().clienteId

    private val _uiState = MutableStateFlow(DetalleClienteUiState())
    val uiState: StateFlow<DetalleClienteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cliente = clienteRepository.getById(clienteId)
            // Balance and status will be computed from pedidos in Phase 4.
            _uiState.value = DetalleClienteUiState(
                cliente = cliente,
                status = ClientStatus.AL_DIA,
                balance = 0.0,
                isLoading = false,
            )
        }
    }
}
