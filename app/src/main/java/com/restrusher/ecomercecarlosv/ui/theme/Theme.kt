package com.restrusher.ecomercecarlosv.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode

val LocalPedidosColors = staticCompositionLocalOf { DarkExtendedColors }

val MaterialTheme.extendedColors: PedidosExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPedidosColors.current

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color.White,
    primaryContainer = DarkAccentTint,
    onPrimaryContainer = DarkAccent,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkText2,
    surfaceContainer = DarkSurface3,
    outline = DarkBorder2,
    outlineVariant = DarkBorder,
    error = DarkRed,
    onError = Color.White,
    errorContainer = DarkRedTint,
    onErrorContainer = DarkRedText,
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentTint,
    onPrimaryContainer = LightAccent,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightText2,
    surfaceContainer = LightSurface3,
    outline = LightBorder2,
    outlineVariant = LightBorder,
    error = LightRed,
    onError = Color.White,
    errorContainer = LightRedTint,
    onErrorContainer = LightRedText,
)

@Composable
fun EcomerceCarlosVTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalPedidosColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
