package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MercadosViewModel @Inject constructor(
    mercadoRepository: MercadoRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _selectedMercadoId = MutableStateFlow<String?>(null)

    val uiState = combine(
        mercadoRepository.getAll(),
        sessionManager.currentUser,
        _selectedMercadoId,
    ) { mercados, user, selectedId ->
        val initials = user?.name
            ?.split(' ')
            ?.filter(String::isNotBlank)
            ?.take(2)
            ?.map { it.first().uppercaseChar() }
            ?.joinToString("") ?: ""
        MercadosUiState(
            mercados = mercados,
            isLoading = false,
            currentUserInitials = initials,
            selectedMercadoId = selectedId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MercadosUiState(isLoading = true),
    )

    fun onMercadoLongPress(mercadoId: String) {
        _selectedMercadoId.value = if (_selectedMercadoId.value == mercadoId) null else mercadoId
    }

    fun clearSelection() {
        _selectedMercadoId.value = null
    }
}
