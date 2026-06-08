package com.restrusher.ecomercecarlosv.data.prefs

import android.content.Context
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UmbralesManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _umbrales = MutableStateFlow(
        Umbrales(
            montoMaximo = prefs.getFloat(KEY_MONTO, 200f).toDouble(),
            diasMaximos = prefs.getInt(KEY_DIAS, 30),
        )
    )
    val umbrales: StateFlow<Umbrales> = _umbrales.asStateFlow()

    fun save(umbrales: Umbrales) {
        _umbrales.value = umbrales
        prefs.edit()
            .putFloat(KEY_MONTO, umbrales.montoMaximo.toFloat())
            .putInt(KEY_DIAS, umbrales.diasMaximos)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "umbrales_prefs"
        private const val KEY_MONTO = "monto_maximo"
        private const val KEY_DIAS = "dias_maximos"
    }
}
