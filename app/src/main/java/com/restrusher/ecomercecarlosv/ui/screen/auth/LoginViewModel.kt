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

// ─── STUB: hardcoded user — remove entirely in Phase 9 ──────────────────────
// TODO (Phase 9): Delete STUB_ADMIN and the two credential constants below.
//   Real auth flows through supabaseClient.auth.signInWith(Email) { ... }
private val STUB_ADMIN = AppUser(
    id = "u1",
    email = "carlos@comercializadora.ve",
    name = "Carlos Villarroel",
    role = UserRole.SUPERUSUARIO,
    isActive = true,
    createdAt = 0L,
)
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginFormState())
    val state: StateFlow<LoginFormState> = _state.asStateFlow()

    init {
        // TODO (Phase 9): Before biometric check, restore an existing Supabase session so
        //   users who were already signed in don't see the login screen on relaunch:
        //     val session = supabaseClient.auth.currentSessionOrNull()
        //     if (session != null && !session.isExpired()) {
        //         val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        //         val user   = userRepository.getById(userId) ?: fetchAndUpsertFromSupabase(userId)
        //         sessionManager.setCurrentUser(user)
        //         onAutoLoginSuccess()   // navigate to HomeRoute via a callback passed into the VM
        //     }
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val deviceReady = BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
            if (deviceReady && userRepository.hasBiometricEnabled()) {
                val user = userRepository.getBiometricEnabledUser()
                _state.value = _state.value.copy(
                    isBiometricEnabled = true,
                    enrolledUserName = user?.name ?: "",
                    enrolledUserEmail = user?.email ?: "",
                    enrolledUserRole = user?.role ?: UserRole.USUARIO,
                    enrolledUserInitials = computeInitials(user?.name ?: ""),
                )
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
            val s = _state.value
            // ─── STUB: replace this entire block with Supabase auth (Phase 9) ──────
            // TODO (Phase 9):
            //   try {
            //       supabaseClient.auth.signInWith(Email) {
            //           email    = s.email.trim()
            //           password = s.password
            //       }
            //       val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            //       val user   = userRepository.getById(userId)
            //                    ?: fetchAndUpsertFromSupabase(userId)  // sync on first device login
            //       sessionManager.setCurrentUser(user)
            //       _state.value = s.copy(isLoading = false)
            //       onSuccess()
            //   } catch (e: AuthException) {
            //       _state.value = s.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
            //   }
            delay(300)
            if (s.email.trim() == "admin" && s.password == "admin") {
                if (userRepository.getById(STUB_ADMIN.id) == null) userRepository.save(STUB_ADMIN)
                val user = userRepository.getById(STUB_ADMIN.id) ?: STUB_ADMIN
                sessionManager.setCurrentUser(user)
                _state.value = s.copy(isLoading = false)
                onSuccess()
            } else {
                _state.value = s.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
            }
            // ─────────────────────────────────────────────────────────────────────
        }
    }

    fun onBiometricSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getBiometricEnabledUser() ?: return@launch
            sessionManager.setCurrentUser(user)
            onSuccess()
        }
    }

    fun onBiometricFailed() { /* no-op */ }

    fun switchToPasswordLogin() {
        _state.value = _state.value.copy(showPasswordLogin = true)
    }

    fun switchToOtherAccount() {
        _state.value = _state.value.copy(
            isBiometricEnabled = false,
            showPasswordLogin = false,
            email = "",
            password = "",
            errorMessage = null,
        )
    }

    private fun computeInitials(name: String) = name
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}
