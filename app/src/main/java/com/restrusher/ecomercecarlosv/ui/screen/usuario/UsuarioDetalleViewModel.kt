package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
                    user = user.toUiModel(currentId),
                    selectedRole = user.role,
                    isLoading = false,
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
        val user = _state.value.user ?: return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            val domainUser = userRepository.getById(user.id) ?: return@launch
            userRepository.save(domainUser.copy(role = _state.value.selectedRole))
            // TODO (Phase 9): Sync role change to Supabase user metadata:
            //   supabaseClient.auth.admin.updateUserById(userId) {
            //       userMetadata = buildJsonObject { put("role", selectedRole.name) }
            //   }
            _state.value = _state.value.copy(isSaving = false)
            onDone()
        }
    }

    fun onDeactivate(onDone: () -> Unit) {
        viewModelScope.launch {
            userRepository.setActive(userId, false)
            // TODO (Phase 9): Also disable the account in Supabase so the user cannot sign in:
            //   supabaseClient.auth.admin.updateUserById(userId) { banned = true }
            //   Run Supabase call first; only update Room on success to keep state consistent.
            onDone()
        }
    }

    fun onDelete(onDone: () -> Unit) {
        _state.value = _state.value.copy(isDeleting = true)
        viewModelScope.launch {
            userRepository.delete(userId)
            // TODO (Phase 9): Also hard-delete the account from Supabase:
            //   supabaseClient.auth.admin.deleteUser(userId)
            //   Run Supabase call first; only delete from Room on success.
            _state.value = _state.value.copy(isDeleting = false)
            onDone()
        }
    }
}
