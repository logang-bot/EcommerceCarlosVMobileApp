package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.annotation.StringRes
import com.restrusher.ecomercecarlosv.domain.model.UserRole

/**
 * [errorMessage] is the Edge Function's own Spanish text, shown when the server sent one;
 * [errorRes] is the fallback for when it did not. The screen prefers the first.
 */
data class UsuarioDetalleUiState(
    val user: UserUiModel? = null,
    val selectedRole: UserRole = UserRole.USUARIO,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    @StringRes val errorRes: Int? = null,
)
