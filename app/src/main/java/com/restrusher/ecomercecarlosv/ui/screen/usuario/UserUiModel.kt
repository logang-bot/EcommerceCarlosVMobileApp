package com.restrusher.ecomercecarlosv.ui.screen.usuario

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole

data class UserUiModel(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val initials: String,
    val isActive: Boolean,
    val isCurrentUser: Boolean,
    val lastSeenLabel: String?,
    val displayRole: String,
)

fun AppUser.toUiModel(currentUserId: String? = null) = UserUiModel(
    id            = id,
    name          = name,
    email         = email,
    role          = role,
    initials      = name.split(' ').filter(String::isNotBlank).take(2).map { it.first().uppercaseChar() }.joinToString(""),
    isActive      = isActive,
    isCurrentUser = id == currentUserId,
    lastSeenLabel = null,
    displayRole   = if (role == UserRole.SUPERUSUARIO) "Super usuario" else "Usuario",
)
