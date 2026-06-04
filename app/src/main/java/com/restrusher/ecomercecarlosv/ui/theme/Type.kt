package com.restrusher.ecomercecarlosv.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import com.restrusher.ecomercecarlosv.R

@OptIn(ExperimentalTextApi::class)
val GeistFontFamily = FontFamily(
    Font(R.font.geist_variable, FontWeight.Light,     variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.geist_variable, FontWeight.Normal,    variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.geist_variable, FontWeight.Medium,    variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.geist_variable, FontWeight.SemiBold,  variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.geist_variable, FontWeight.Bold,      variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.geist_variable, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    Font(R.font.geist_italic_variable, FontWeight.Normal,   FontStyle.Italic, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.geist_italic_variable, FontWeight.Medium,   FontStyle.Italic, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.geist_italic_variable, FontWeight.SemiBold, FontStyle.Italic, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

@OptIn(ExperimentalTextApi::class)
val GeistMonoFontFamily = FontFamily(
    Font(R.font.geist_mono_variable, FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.geist_mono_variable, FontWeight.Medium,   variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.geist_mono_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)
