package com.restrusher.ecomercecarlosv.ui.screen.usuario

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class UsuarioDetalleUiState(
    val user: UserUiModel? = null,
    val selectedRole: UserRole = UserRole.USUARIO,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
)
