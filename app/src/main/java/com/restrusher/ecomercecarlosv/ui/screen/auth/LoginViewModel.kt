package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Replace hardcoded admin/admin check with Supabase auth — see docs/features/auth.md
private val STUB_ADMIN = AppUser(
    id        = "u1",
    email     = "carlos@comercializadora.ve",
    name      = "Carlos Villarroel",
    role      = UserRole.SUPERUSUARIO,
    isActive  = true,
    createdAt = 0L,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginFormState())
    val state: StateFlow<LoginFormState> = _state.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val deviceReady = BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
            if (deviceReady && userRepository.hasBiometricEnabled()) {
                _state.value = _state.value.copy(isBiometricEnabled = true)
            }
        }
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            delay(300)
            val s = _state.value
            if (s.email.trim() == "admin" && s.password == "admin") {
                // Ensure stub user exists in DB (first run)
                if (userRepository.getById(STUB_ADMIN.id) == null) {
                    userRepository.save(STUB_ADMIN)
                }
                // Load from DB to pick up any saved biometricEnabledAt
                val user = userRepository.getById(STUB_ADMIN.id) ?: STUB_ADMIN
                sessionManager.setCurrentUser(user)
                _state.value = s.copy(isLoading = false)
                onSuccess()
            } else {
                _state.value = s.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
            }
        }
    }
}
