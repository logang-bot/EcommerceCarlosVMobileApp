package com.restrusher.ecomercecarlosv.ui.screen.mercado

data class CreateMercadoFormState(
    val name: String = "",
    val address: String = "",
    val nameError: Boolean = false,
    val isLoading: Boolean = false,
)
