package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    private val userRepository: UserRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            userRepository.syncAllFromRemote()
            _isSyncing.value = false
        }
    }

    val uiState = combine(
        userRepository.getAll(),
        sessionManager.currentUser,
        _isSyncing,
    ) { users, currentUser, isSyncing ->
        val mapped = users.map { it.toUiModel(currentUser?.id) }
        GestionUsuariosUiState(
            superUsuarios = mapped.filter { it.role == UserRole.SUPERUSUARIO },
            usuarios      = mapped.filter { it.role == UserRole.USUARIO },
            invitados     = mapped.filter { it.role == UserRole.INVITADO },
            isLoading     = isSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GestionUsuariosUiState(),
    )
}
