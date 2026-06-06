package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.net.Uri
import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class EditarPerfilUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.USUARIO,
    val initials: String = "",
    val photoUri: Uri? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
)
