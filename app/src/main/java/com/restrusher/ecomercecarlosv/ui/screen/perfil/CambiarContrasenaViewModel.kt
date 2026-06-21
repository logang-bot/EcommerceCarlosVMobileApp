package com.restrusher.ecomercecarlosv.ui.screen.perfil

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.di.AdminClient
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.CambiarContrasenaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CambiarContrasenaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val supabase: SupabaseClient,
    @AdminClient private val adminClient: SupabaseClient,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CambiarContrasenaRoute>()
    private val userId: String = route.userId
    private val isSelf: Boolean = route.isSelf

    private val _state = MutableStateFlow(CambiarContrasenaUiState(isSelf = isSelf))
    val state: StateFlow<CambiarContrasenaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = if (isSelf) {
                sessionManager.currentUser.value
            } else {
                userRepository.getById(userId)
            }
            if (user != null) {
                _state.value = _state.value.copy(
                    targetName  = user.name,
                    targetEmail = user.email,
                    targetRole  = user.role,
                )
            }
        }
    }

    fun onCurrentPasswordChange(value: String) {
        _state.value = _state.value.copy(currentPassword = value, errorMessage = null)
    }

    fun onNewPasswordChange(value: String) {
        _state.value = _state.value.copy(newPassword = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun onSave() {
        val s = _state.value
        if (!s.isValid) return
        _state.value = s.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                if (isSelf) {
                    supabase.auth.signInWith(Email) {
                        email = s.targetEmail
                        password = s.currentPassword
                    }
                    supabase.auth.updateUser { password = s.newPassword }
                } else {
                    adminClient.auth.admin.updateUserById(userId) { password = s.newPassword }
                }
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Error al cambiar la contraseña",
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
