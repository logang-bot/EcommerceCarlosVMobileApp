package com.restrusher.ecomercecarlosv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalPedidosColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
