package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleMercadoViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mercadoId: String = savedStateHandle.toRoute<DetalleMercadoRoute>().mercadoId

    val uiState = combine(
        mercadoRepository.getByIdFlow(mercadoId),
        sessionManager.currentUser,
    ) { mercado, user ->
        DetalleMercadoUiState(
            mercado = mercado,
            mercadoId = mercadoId,
            isLoading = false,
            canWrite = user?.role != UserRole.INVITADO,
        )
    }.stateIn(
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
