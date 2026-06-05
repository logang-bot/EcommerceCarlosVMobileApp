package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.net.Uri

data class CreateMercadoFormState(
    val name: String = "",
    val address: String = "",
    val mapsUrl: String = "",
    val nameError: Boolean = false,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val photoUri: Uri? = null,
)
