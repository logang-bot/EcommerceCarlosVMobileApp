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

val DarkAccent      = Color(0xFF5B8DEF)
val DarkAccentPress = Color(0xFF4878DD)
val DarkAccentTint  = Color(0x265B8DEF) // 15% accent
val DarkAccentSoft  = Color(0x1A5B8DEF) // 10% accent

val DarkGreen       = Color(0xFF36C880)
val DarkGreenText   = Color(0xFF4FE09A)
val DarkGreenTint   = Color(0x2436C880) // 14%
val DarkAmber       = Color(0xFFE7B23E)
val DarkAmberText   = Color(0xFFF2C357)
val DarkAmberTint   = Color(0x24E7B23E) // 14%
val DarkRed         = Color(0xFFF05A50)
val DarkRedText     = Color(0xFFFF7268)
val DarkRedTint     = Color(0x24F05A50) // 14%

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

val LightAccent      = Color(0xFF2F6BE0)
val LightAccentPress = Color(0xFF2557C4)
val LightAccentTint  = Color(0x1C2F6BE0) // 11%
val LightAccentSoft  = Color(0x122F6BE0) // 7%

val LightGreen       = Color(0xFF1FA45C)
val LightGreenText   = Color(0xFF157A41)
val LightGreenTint   = Color(0x1F1FA45C) // 12%
val LightAmber       = Color(0xFFC98A00)
val LightAmberText   = Color(0xFF946400)
val LightAmberTint   = Color(0x21C98A00) // 13%
val LightRed         = Color(0xFFDC3B30)
val LightRedText     = Color(0xFFC42A20)
val LightRedTint     = Color(0x1ADC3B30) // 10%

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
    val accentSoft: Color,
    val green: Color,
    val greenText: Color,
    val greenTint: Color,
    val amber: Color,
    val amberText: Color,
    val amberTint: Color,
    val redText: Color,
    val redTint: Color,
)

val DarkExtendedColors = PedidosExtendedColors(
    surface2 = DarkSurface2,
    surface3 = DarkSurface3,
    elevated = DarkElevated,
    border = DarkBorder,
    border2 = DarkBorder2,
    border3 = DarkBorder3,
    text2 = DarkText2,
    text3 = DarkText3,
    text4 = DarkText4,
    accentPress = DarkAccentPress,
    accentSoft = DarkAccentSoft,
    green = DarkGreen,
    greenText = DarkGreenText,
    greenTint = DarkGreenTint,
    amber = DarkAmber,
    amberText = DarkAmberText,
    amberTint = DarkAmberTint,
    redText = DarkRedText,
    redTint = DarkRedTint,
)

val LightExtendedColors = PedidosExtendedColors(
    surface2 = LightSurface2,
    surface3 = LightSurface3,
    elevated = LightElevated,
    border = LightBorder,
    border2 = LightBorder2,
    border3 = LightBorder3,
    text2 = LightText2,
    text3 = LightText3,
    text4 = LightText4,
    accentPress = LightAccentPress,
    accentSoft = LightAccentSoft,
    green = LightGreen,
    greenText = LightGreenText,
    greenTint = LightGreenTint,
    amber = LightAmber,
    amberText = LightAmberText,
    amberTint = LightAmberTint,
    redText = LightRedText,
    redTint = LightRedTint,
)
