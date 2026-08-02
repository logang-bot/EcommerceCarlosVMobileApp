package com.restrusher.ecomercecarlosv.ui.screen.auth

import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.usecase.DeviceHandover

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAccountDisabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val showPasswordLogin: Boolean = false,
    val showForgetDialog: Boolean = false,
    /** False once the stored refresh token is gone: the fingerprint has nothing left to present. */
    val canUseFingerprint: Boolean = true,
    val handover: DeviceHandover.ConfirmationRequired? = null,
    val enrolledUserName: String = "",
    val enrolledUserFirstName: String = "",
    val enrolledUserEmail: String = "",
    val enrolledUserRole: UserRole = UserRole.USUARIO,
    val enrolledUserInitials: String = "",
    val enrolledUserPhotoUrl: String? = null,
)
