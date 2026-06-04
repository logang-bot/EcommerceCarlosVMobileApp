package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MercadosViewModel @Inject constructor(
    mercadoRepository: MercadoRepository,
) : ViewModel() {

    val uiState = mercadoRepository
        .getAll()
        .map { MercadosUiState(mercados = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MercadosUiState(isLoading = true),
        )
}
