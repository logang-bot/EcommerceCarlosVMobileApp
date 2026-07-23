package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.data.remote.AdminUserService
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.UsuarioDetalleRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsuarioDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val adminUserService: AdminUserService,
) : ViewModel() {

    private val userId: String = savedStateHandle.toRoute<UsuarioDetalleRoute>().userId

    private val _state = MutableStateFlow(UsuarioDetalleUiState())
    val state: StateFlow<UsuarioDetalleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getById(userId)
            val currentId = sessionManager.currentUser.value?.id
            if (user != null) {
                _state.value = UsuarioDetalleUiState(
                    user         = user.toUiModel(currentId),
                    selectedRole = user.role,
                    isLoading    = false,
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun onRoleChange(role: UserRole) {
        _state.value = _state.value.copy(selectedRole = role)
    }

    fun onSaveRole(onDone: () -> Unit) {
        val uiUser = _state.value.user ?: return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            val domainUser = userRepository.getById(uiUser.id) ?: run {
                _state.value = _state.value.copy(isSaving = false)
                return@launch
            }
            val newRole = _state.value.selectedRole
            try {
                // Edge Function updates the auth metadata + `users` row server-side.
                adminUserService.updateRole(userId, newRole.name)
            } catch (_: Exception) { /* will sync later */ }
            userRepository.save(domainUser.copy(role = newRole))
            _state.value = _state.value.copy(isSaving = false)
            onDone()
        }
    }

    fun onDeactivate(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                // Edge Function bans the auth user (~100-year indefinite disable) and
                // sets is_active = false in the `users` row server-side.
                adminUserService.setActive(userId, false)
            } catch (_: Exception) { /* will sync later */ }
            userRepository.setActive(userId, false)
            onDone()
        }
    }

    fun onActivate(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                adminUserService.setActive(userId, true)
            } catch (_: Exception) { /* will sync later */ }
            userRepository.setActive(userId, true)
            onDone()
        }
    }

    fun onDelete(onDone: () -> Unit) {
        _state.value = _state.value.copy(isDeleting = true)
        viewModelScope.launch {
            try {
                // Edge Function deletes the auth account + `users` row server-side.
                adminUserService.deleteUser(userId)
            } catch (_: Exception) { /* will sync later */ }
            userRepository.delete(userId)
            _state.value = _state.value.copy(isDeleting = false)
            onDone()
        }
    }
}
