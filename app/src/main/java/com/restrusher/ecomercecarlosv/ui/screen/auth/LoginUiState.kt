package com.restrusher.ecomercecarlosv.ui.screen.auth

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricEnabled: Boolean = false,
)
