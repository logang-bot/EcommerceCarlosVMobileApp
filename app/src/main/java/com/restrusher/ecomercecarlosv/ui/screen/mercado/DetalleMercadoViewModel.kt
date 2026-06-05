package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleMercadoViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mercadoId: String = savedStateHandle.toRoute<DetalleMercadoRoute>().mercadoId

    private val _uiState = MutableStateFlow(DetalleMercadoUiState())
    val uiState: StateFlow<DetalleMercadoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val mercado = mercadoRepository.getById(mercadoId)
            _uiState.value = DetalleMercadoUiState(mercado = mercado, mercadoId = mercadoId, isLoading = false)
        }
    }

    fun onDelete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            mercadoRepository.delete(mercadoId)
            onSuccess()
        }
    }
}
