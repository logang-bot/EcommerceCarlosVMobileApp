package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilUiState())
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = sessionManager.currentUser.value ?: return@launch

            val biometricStatus = BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            val isAvailable = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS

            val allUsers = userRepository.getAll().first()
            val superCount = allUsers.count { it.role == UserRole.SUPERUSUARIO }
            val totalCount = allUsers.size

            _state.value = PerfilUiState(
                name                 = user.name,
                email                = user.email,
                phone                = user.phone ?: "",
                role                 = user.role,
                initials             = computeInitials(user.name),
                isBiometricAvailable = isAvailable,
                isBiometricEnrolled  = user.biometricEnabledAt != null,
                biometricEnabledDate = user.biometricEnabledAt?.let(::formatDate),
                teamSummary          = "$totalCount usuarios · $superCount super usuarios",
                isLoading            = false,
            )
        }
    }

    fun disableBiometric() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            userRepository.setBiometricEnabled(user.id, null)
            sessionManager.setCurrentUser(user.copy(biometricEnabledAt = null))
            _state.value = _state.value.copy(
                isBiometricEnrolled  = false,
                biometricEnabledDate = null,
            )
        }
    }

    fun onBiometricAuthSuccess() {
        val user = sessionManager.currentUser.value ?: return
        val enabledAt = System.currentTimeMillis()
        viewModelScope.launch {
            userRepository.setBiometricEnabled(user.id, enabledAt)
            sessionManager.setCurrentUser(user.copy(biometricEnabledAt = enabledAt))
            _state.value = _state.value.copy(
                isBiometricEnrolled  = true,
                biometricEnabledDate = formatDate(enabledAt),
            )
        }
    }

    fun onBiometricAuthFailed() {
        // No-op: prompt was dismissed or failed — leave state unchanged.
    }

    fun logout(onLoggedOut: () -> Unit) {
        sessionManager.clearSession()
        onLoggedOut()
    }

    private fun computeInitials(name: String) = name
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

    private fun formatDate(epochMillis: Long): String =
        SimpleDateFormat("d 'de' MMMM", Locale("es")).format(Date(epochMillis))
}
