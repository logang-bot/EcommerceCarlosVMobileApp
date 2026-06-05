package com.restrusher.ecomercecarlosv.ui.screen.perfil

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class PerfilUiState(
    val name: String = "",
    val email: String = "",
    val businessName: String = "Comercializadora Carlos V",
    val role: UserRole = UserRole.USUARIO,
    val initials: String = "",
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnrolled: Boolean = false,
    val biometricEnabledDate: String? = null,
    val teamSummary: String = "",
    val isLoading: Boolean = true,
)
