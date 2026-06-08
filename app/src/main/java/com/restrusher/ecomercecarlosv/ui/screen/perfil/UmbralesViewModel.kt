package com.restrusher.ecomercecarlosv.ui.screen.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.prefs.UmbralesManager
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UmbralesViewModel @Inject constructor(
    private val umbralesManager: UmbralesManager,
) : ViewModel() {

    private val initial = umbralesManager.umbrales.value

    private val _montoText = MutableStateFlow(formatMonto(initial.montoMaximo))
    private val _diasText = MutableStateFlow(initial.diasMaximos.toString())

    val montoText: StateFlow<String> = _montoText.asStateFlow()
    val diasText: StateFlow<String> = _diasText.asStateFlow()

    val canSave: StateFlow<Boolean> = combine(_montoText, _diasText) { m, d ->
        m.replace(",", ".").toDoubleOrNull() != null && d.toIntOrNull()?.let { it >= 1 } == true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onMontoChange(text: String) { _montoText.value = text }

    fun onDiasChange(text: String) {
        _diasText.value = text.filter { it.isDigit() }
    }

    fun save() {
        val monto = _montoText.value.replace(",", ".").toDoubleOrNull()?.coerceAtLeast(0.0) ?: return
        val dias = _diasText.value.toIntOrNull()?.coerceAtLeast(1) ?: return
        umbralesManager.save(Umbrales(montoMaximo = monto, diasMaximos = dias))
    }

    private fun formatMonto(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else "%.2f".format(value)
}
