package com.restrusher.ecomercecarlosv.ui.screen.producto

import android.net.Uri

data class CreateProductoFormState(
    val name: String = "",
    val nameError: Boolean = false,
    val description: String = "",
    val price: String = "",
    val priceError: Boolean = false,
    val photoUri: Uri? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val showDeleteDialog: Boolean = false,
)
