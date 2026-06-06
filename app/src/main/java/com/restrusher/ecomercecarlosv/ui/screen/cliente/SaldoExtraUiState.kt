package com.restrusher.ecomercecarlosv.ui.screen.cliente

data class SaldoExtraUiState(
    val clienteName: String = "",
    val description: String = "",
    val descriptionError: Boolean = false,
    val amount: String = "",
    val amountError: Boolean = false,
    val date: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = description.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true && !isSaving
}
