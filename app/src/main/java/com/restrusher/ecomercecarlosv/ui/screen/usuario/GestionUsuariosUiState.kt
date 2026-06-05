package com.restrusher.ecomercecarlosv.ui.screen.usuario

data class GestionUsuariosUiState(
    val superUsuarios: List<UserUiModel> = emptyList(),
    val usuarios: List<UserUiModel> = emptyList(),
    val isLoading: Boolean = true,
) {
    val totalCount get() = superUsuarios.size + usuarios.size
    val summary get() = "$totalCount usuarios · ${superUsuarios.size} super usuarios"
}
