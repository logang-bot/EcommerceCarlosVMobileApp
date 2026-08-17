package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleMercadoViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    clienteRepository: ClienteRepository,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mercadoId: String = savedStateHandle.toRoute<DetalleMercadoRoute>().mercadoId

    private val _showDeleteDialog = MutableStateFlow(false)

    val uiState = combine(
        mercadoRepository.getByIdFlow(mercadoId),
        sessionManager.currentUser,
        clienteRepository.countByMercado(mercadoId),
        _showDeleteDialog,
    ) { mercado, user, clienteCount, showDeleteDialog ->
        DetalleMercadoUiState(
            mercado = mercado,
            mercadoId = mercadoId,
            isLoading = false,
            canWrite = user?.role != UserRole.INVITADO,
            showDeleteDialog = showDeleteDialog,
            clienteCount = clienteCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleMercadoUiState(isLoading = true),
    )

    fun onShowDeleteDialog() { _showDeleteDialog.value = true }

    fun onDismissDeleteDialog() { _showDeleteDialog.value = false }

    fun onDelete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _showDeleteDialog.value = false
            mercadoRepository.delete(mercadoId)
            onSuccess()
        }
    }
}
