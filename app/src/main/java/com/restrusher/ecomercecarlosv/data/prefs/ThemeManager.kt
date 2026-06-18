package com.restrusher.ecomercecarlosv.data.prefs

import android.content.Context
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.getOrElse(prefs.getInt(KEY_THEME, ThemeMode.SYSTEM.ordinal)) { ThemeMode.SYSTEM }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putInt(KEY_THEME, mode.ordinal).apply()
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME = "theme_mode"
    }
}
