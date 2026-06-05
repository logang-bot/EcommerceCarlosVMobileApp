package com.restrusher.ecomercecarlosv.ui.screen.usuario

import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class InvitarUsuarioFormState(
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USUARIO,
    val nameError: Boolean = false,
    val emailError: Boolean = false,
    val isSending: Boolean = false,
    val sent: Boolean = false,
)
