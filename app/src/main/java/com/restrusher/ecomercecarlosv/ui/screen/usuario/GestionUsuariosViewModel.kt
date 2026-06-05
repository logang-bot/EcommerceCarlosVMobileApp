package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    userRepository: UserRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val uiState = combine(
        userRepository.getAll(),
        sessionManager.currentUser,
    ) { users, currentUser ->
        val mapped = users.map { it.toUiModel(currentUser?.id) }
        GestionUsuariosUiState(
            superUsuarios = mapped.filter { it.role == UserRole.SUPERUSUARIO },
            usuarios = mapped.filter { it.role == UserRole.USUARIO },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GestionUsuariosUiState(),
    )
}
