package com.restrusher.ecomercecarlosv.ui.screen.perfil

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class CambiarContrasenaUiState(
    val targetName: String = "",
    val targetEmail: String = "",
    val targetRole: UserRole = UserRole.USUARIO,
    val isSelf: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val passwordMismatch: Boolean get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword
    val meetsLength: Boolean get() = newPassword.length >= 8
    val meetsNumber: Boolean get() = newPassword.any { it.isDigit() }
    val meetsCasing: Boolean get() = newPassword.any { it.isUpperCase() } && newPassword.any { it.isLowerCase() }
    val isValid: Boolean
        get() = meetsLength && meetsNumber && meetsCasing &&
            newPassword.isNotEmpty() && newPassword == confirmPassword &&
            (!isSelf || currentPassword.isNotEmpty())

    val targetFirstName: String get() = targetName.split(' ').firstOrNull { it.isNotBlank() } ?: targetName
}
