package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.prefs.ThemeManager
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
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
    private val umbralesRepository: UmbralesRepository,
    private val themeManager: ThemeManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilUiState())
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            umbralesRepository.getUmbrales().collect { u ->
                _state.value = _state.value.copy(umbralesSummary = formatUmbralesSummary(u))
            }
        }
        viewModelScope.launch {
            themeManager.themeMode.collect { mode ->
                _state.value = _state.value.copy(themeMode = mode)
            }
        }
    }

    private fun formatUmbralesSummary(u: Umbrales): String {
        val monto = if (u.montoMaximo == u.montoMaximo.toLong().toDouble()) {
            u.montoMaximo.toLong().toString()
        } else {
            "%.2f".format(u.montoMaximo)
        }
        return "Crítico desde Bs. $monto o ${u.diasMaximos} días sin pagar"
    }

    private fun loadProfile() {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                user ?: return@collect

                val biometricStatus = BiometricManager.from(context)
                    .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                val isAvailable = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS

                val allUsers = userRepository.getAll().first()
                val superCount = allUsers.count { it.role == UserRole.SUPERUSUARIO }
                val totalCount = allUsers.size

                _state.value = PerfilUiState(
                    userId               = user.id,
                    name                 = user.name,
                    email                = user.email,
                    phone                = user.phone ?: "",
                    role                 = user.role,
                    initials             = computeInitials(user.name),
                    photoUrl             = user.photoUrl,
                    isBiometricAvailable = isAvailable,
                    isBiometricEnrolled  = user.biometricEnabledAt != null,
                    biometricEnabledDate = user.biometricEnabledAt?.let(::formatDate),
                    teamSummary          = "$totalCount usuarios · $superCount super usuarios",
                    isLoading            = false,
                )
            }
        }
    }

    fun disableBiometric() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            userRepository.setBiometricEnabled(user.id, null)
            sessionManager.setCurrentUser(user.copy(biometricEnabledAt = null))
            // The stored refresh token stays: the user is still signed in, and it is what keeps
            // their session alive. Signing out later revokes it, since they are no longer enrolled.
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

    fun setTheme(mode: ThemeMode) = themeManager.setTheme(mode)

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            sessionManager.signOut()
            onLoggedOut()
        }
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
