package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.data.remote.AdminOperationException
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
        _state.value = _state.value.copy(selectedRole = role, errorMessage = null, errorRes = null)
    }

    fun onSaveRole(onDone: () -> Unit) {
        val uiUser = _state.value.user ?: return
        startOperation(isDeleting = false)
        viewModelScope.launch {
            val domainUser = userRepository.getById(uiUser.id) ?: run {
                _state.value = _state.value.copy(isSaving = false)
                return@launch
            }
            val newRole = _state.value.selectedRole
            // Edge Function updates the auth metadata + `users` row server-side. Nothing retries
            // this — it is not a queued sync operation — so a failure must not reach Room.
            if (!runAdminOp(R.string.usuario_detalle_error_rol) { adminUserService.updateRole(userId, newRole.name) }) return@launch
            userRepository.save(domainUser.copy(role = newRole))
            _state.value = _state.value.copy(isSaving = false)
            onDone()
        }
    }

    fun onDeactivate(onDone: () -> Unit) = setActive(isActive = false, onDone = onDone)

    fun onActivate(onDone: () -> Unit) = setActive(isActive = true, onDone = onDone)

    // Edge Function bans the auth user (~100-year indefinite disable) or lifts the ban, and writes
    // is_active in the `users` row server-side.
    private fun setActive(isActive: Boolean, onDone: () -> Unit) {
        startOperation(isDeleting = false)
        viewModelScope.launch {
            if (!runAdminOp(R.string.usuario_detalle_error_estado) { adminUserService.setActive(userId, isActive) }) return@launch
            userRepository.setActive(userId, isActive)
            _state.value = _state.value.copy(isSaving = false)
            onDone()
        }
    }

    fun onDelete(onDone: () -> Unit) {
        startOperation(isDeleting = true)
        viewModelScope.launch {
            // Edge Function deletes the auth account + `users` row server-side.
            if (!runAdminOp(R.string.usuario_detalle_error_eliminar) { adminUserService.deleteUser(userId) }) return@launch
            userRepository.delete(userId)
            _state.value = _state.value.copy(isDeleting = false)
            onDone()
        }
    }

    private fun startOperation(isDeleting: Boolean) {
        _state.value = _state.value.copy(
            isSaving     = !isDeleting,
            isDeleting   = isDeleting,
            errorMessage = null,
            errorRes     = null,
        )
    }

    /** Returns false when the call failed, in which case the error is already on screen. */
    private suspend fun runAdminOp(@StringRes fallbackRes: Int, block: suspend () -> Unit): Boolean = try {
        block()
        true
    } catch (e: AdminOperationException) {
        _state.value = _state.value.copy(
            isSaving     = false,
            isDeleting   = false,
            errorMessage = e.serverMessage,
            errorRes     = fallbackRes,
        )
        false
    }
}
