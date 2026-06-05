package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MercadosViewModel @Inject constructor(
    mercadoRepository: MercadoRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val uiState = combine(
        mercadoRepository.getAll(),
        sessionManager.currentUser,
    ) { mercados, user ->
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
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MercadosUiState(isLoading = true),
    )
}
