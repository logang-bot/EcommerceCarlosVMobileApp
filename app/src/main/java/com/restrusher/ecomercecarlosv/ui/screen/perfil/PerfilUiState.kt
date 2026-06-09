package com.restrusher.ecomercecarlosv.ui.screen.perfil

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class PerfilUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.USUARIO,
    val initials: String = "",
    val photoUrl: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnrolled: Boolean = false,
    val biometricEnabledDate: String? = null,
    val teamSummary: String = "",
    val umbralesSummary: String = "Crítico desde Bs. 200 o 30 días sin pagar",
    val isLoading: Boolean = true,
)
