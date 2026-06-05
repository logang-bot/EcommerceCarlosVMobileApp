package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.net.Uri

data class CreateClienteFormState(
    val name: String = "",
    val nameError: Boolean = false,
    val description: String = "",
    val descriptionError: Boolean = false,
    val phones: List<String> = listOf(""),
    val mapsUrl: String = "",
    val photoUri: Uri? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
)
