package com.restrusher.ecomercecarlosv.ui.screen.perfil

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class EditarPerfilUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.USUARIO,
    val initials: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
)
