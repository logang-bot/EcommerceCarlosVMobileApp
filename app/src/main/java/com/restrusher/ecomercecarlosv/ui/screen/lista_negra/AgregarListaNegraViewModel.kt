package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.presentation.screens.AgregarListaNegraRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgregarListaNegraViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<AgregarListaNegraRoute>().clienteId

    private val _uiState = MutableStateFlow(AgregarListaNegraUiState())
    val uiState: StateFlow<AgregarListaNegraUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cliente = clienteRepository.getById(clienteId)
            _uiState.value = _uiState.value.copy(
                clienteId = clienteId,
                clienteName = cliente?.name ?: "",
                isLoading = false,
            )
        }
    }

    fun onAmountChange(v: String) { _uiState.value = _uiState.value.copy(manualAmount = v) }
    fun onReasonChange(v: String) { _uiState.value = _uiState.value.copy(reason = v) }

    fun onConfirm(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (!s.canConfirm) return
        _uiState.value = s.copy(isSaving = true)
        viewModelScope.launch {
            clienteRepository.blacklist(
                id = clienteId,
                reason = s.reason,
                balance = s.manualAmount.toDoubleOrNull() ?: 0.0,
                at = System.currentTimeMillis(),
            )
            onSuccess()
        }
    }
}
