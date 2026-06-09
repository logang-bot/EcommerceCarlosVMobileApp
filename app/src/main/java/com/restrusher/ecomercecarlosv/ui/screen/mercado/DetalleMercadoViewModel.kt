package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleMercadoViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mercadoId: String = savedStateHandle.toRoute<DetalleMercadoRoute>().mercadoId

    val uiState = mercadoRepository.getByIdFlow(mercadoId)
        .map { mercado -> DetalleMercadoUiState(mercado = mercado, mercadoId = mercadoId, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetalleMercadoUiState(isLoading = true),
        )

    fun onDelete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            mercadoRepository.delete(mercadoId)
            onSuccess()
        }
    }
}
