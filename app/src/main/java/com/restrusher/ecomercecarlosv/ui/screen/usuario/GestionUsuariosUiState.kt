package com.restrusher.ecomercecarlosv.ui.screen.usuario

data class GestionUsuariosUiState(
    val superUsuarios: List<UserUiModel> = emptyList(),
    val usuarios: List<UserUiModel> = emptyList(),
    val invitados: List<UserUiModel> = emptyList(),
    val isLoading: Boolean = true,
) {
    val totalCount get() = superUsuarios.size + usuarios.size + invitados.size
    val summary get() = "$totalCount miembros · ${superUsuarios.size} super usuarios"
}
