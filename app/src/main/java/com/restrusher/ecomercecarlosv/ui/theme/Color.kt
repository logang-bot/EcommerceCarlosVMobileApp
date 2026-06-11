package com.restrusher.ecomercecarlosv.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark theme ─────────────────────────────────────────────────────────
val DarkBackground  = Color(0xFF0E0E10)
val DarkSurface     = Color(0xFF161618)
val DarkSurface2    = Color(0xFF1C1C1F)
val DarkSurface3    = Color(0xFF232327)
val DarkElevated    = Color(0xFF26262B)

val DarkBorder      = Color(0x12FFFFFF) // 7% white
val DarkBorder2     = Color(0x1FFFFFFF) // 12% white
val DarkBorder3     = Color(0x2EFFFFFF) // 18% white

val DarkText        = Color(0xFFF3F3F4)
val DarkText2       = Color(0xFF9B9BA3)
val DarkText3       = Color(0xFF66666E)
val DarkText4       = Color(0xFF4A4A51)

// Carlos V brand green (replaces slate blue)
val DarkAccent      = Color(0xFF2FA24E)
val DarkAccentPress = Color(0xFF268241)
val DarkAccentTint  = Color(0x292FA24E) // 16%
val DarkAccentSoft  = Color(0x1A2FA24E) // 10%

// Banana yellow — superuser badge / shield icon
val DarkBanana      = Color(0xFFF4C20D)
val DarkBananaText  = Color(0xFFF6CE36)
val DarkBananaTint  = Color(0x29F4C20D) // 16%
val DarkOnBanana    = Color(0xFF2C2400)

val DarkGreen       = Color(0xFF2BC47D)
val DarkGreenText   = Color(0xFF54DE9A)
val DarkGreenTint   = Color(0x242BC47D) // 14%
val DarkAmber       = Color(0xFFE7B23E)
val DarkAmberText   = Color(0xFFF2C357)
val DarkAmberTint   = Color(0x24E7B23E) // 14%
val DarkRed         = Color(0xFFF05A50)
val DarkRedText     = Color(0xFFFF7268)
val DarkRedTint     = Color(0x24F05A50) // 14%
val DarkBlue        = Color(0xFF4C8DF5)
val DarkBlueText    = Color(0xFF7FB0FF)
val DarkBlueTint    = Color(0x294C8DF5) // rgba(76,141,245,0.16)

// ── Light theme ────────────────────────────────────────────────────────
val LightBackground  = Color(0xFFF4F4F6)
val LightSurface     = Color(0xFFFFFFFF)
val LightSurface2    = Color(0xFFF1F1F3)
val LightSurface3    = Color(0xFFE7E7EB)
val LightElevated    = Color(0xFFFFFFFF)

val LightBorder      = Color(0x14121218) // 8% dark
val LightBorder2     = Color(0x24121218) // 14% dark
val LightBorder3     = Color(0x3D121218) // 24% dark

val LightText        = Color(0xFF18181C)
val LightText2       = Color(0xFF5A5A64)
val LightText3       = Color(0xFF8A8A93)
val LightText4       = Color(0xFFB2B2BA)

val LightAccent      = Color(0xFF1E7D38)
val LightAccentPress = Color(0xFF186430)
val LightAccentTint  = Color(0x1C1E7D38) // 11%
val LightAccentSoft  = Color(0x121E7D38) // 7%

val LightBanana      = Color(0xFFE9B500)
val LightBananaText  = Color(0xFF8A6A00)
val LightBananaTint  = Color(0x29E9B500) // 16%
val LightOnBanana    = Color(0xFF2C2400)

val LightGreen       = Color(0xFF1B9D55)
val LightGreenText   = Color(0xFF137A40)
val LightGreenTint   = Color(0x1F1B9D55) // 12%
val LightAmber       = Color(0xFFC98A00)
val LightAmberText   = Color(0xFF946400)
val LightAmberTint   = Color(0x21C98A00) // 13%
val LightRed         = Color(0xFFDC3B30)
val LightRedText     = Color(0xFFC42A20)
val LightRedTint     = Color(0x1ADC3B30) // 10%
val LightBlue        = Color(0xFF2563EB)
val LightBlueText    = Color(0xFF1D55C2)
val LightBlueTint    = Color(0x1C2563EB) // rgba(37,99,235,0.11)

// ── Extended colors (not covered by Material3 slots) ───────────────────
data class PedidosExtendedColors(
    val surface2: Color,
    val surface3: Color,
    val elevated: Color,
    val border: Color,
    val border2: Color,
    val border3: Color,
    val text2: Color,
    val text3: Color,
    val text4: Color,
    val accentPress: Color,
    val accentTint: Color,
    val accentSoft: Color,
    val banana: Color,
    val bananaText: Color,
    val bananaTint: Color,
    val onBanana: Color,
    val green: Color,
    val greenText: Color,
    val greenTint: Color,
    val amber: Color,
    val amberText: Color,
    val amberTint: Color,
    val redText: Color,
    val redTint: Color,
    val blue: Color,
    val blueText: Color,
    val blueTint: Color,
)

val DarkExtendedColors = PedidosExtendedColors(
    surface2     = DarkSurface2,
    surface3     = DarkSurface3,
    elevated     = DarkElevated,
    border       = DarkBorder,
    border2      = DarkBorder2,
    border3      = DarkBorder3,
    text2        = DarkText2,
    text3        = DarkText3,
    text4        = DarkText4,
    accentPress  = DarkAccentPress,
    accentTint   = DarkAccentTint,
    accentSoft   = DarkAccentSoft,
    banana       = DarkBanana,
    bananaText   = DarkBananaText,
    bananaTint   = DarkBananaTint,
    onBanana     = DarkOnBanana,
    green        = DarkGreen,
    greenText    = DarkGreenText,
    greenTint    = DarkGreenTint,
    amber        = DarkAmber,
    amberText    = DarkAmberText,
    amberTint    = DarkAmberTint,
    redText      = DarkRedText,
    redTint      = DarkRedTint,
    blue         = DarkBlue,
    blueText     = DarkBlueText,
    blueTint     = DarkBlueTint,
)

val LightExtendedColors = PedidosExtendedColors(
    surface2     = LightSurface2,
    surface3     = LightSurface3,
    elevated     = LightElevated,
    border       = LightBorder,
    border2      = LightBorder2,
    border3      = LightBorder3,
    text2        = LightText2,
    text3        = LightText3,
    text4        = LightText4,
    accentPress  = LightAccentPress,
    accentTint   = LightAccentTint,
    accentSoft   = LightAccentSoft,
    banana       = LightBanana,
    bananaText   = LightBananaText,
    bananaTint   = LightBananaTint,
    onBanana     = LightOnBanana,
    green        = LightGreen,
    greenText    = LightGreenText,
    greenTint    = LightGreenTint,
    amber        = LightAmber,
    amberText    = LightAmberText,
    amberTint    = LightAmberTint,
    redText      = LightRedText,
    redTint      = LightRedTint,
    blue         = LightBlue,
    blueText     = LightBlueText,
    blueTint     = LightBlueTint,
)
