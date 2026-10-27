package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object ColorDefinition {
    val BgLanguage = Color(0x4D727272)
    val Color5A5D5D = Color(0xFF5A5A5D)
    val ColorFFD755 = Color(0xFFFFD755)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // ButtonApp Colors
    val ButtonOkBg = Color(0xFF0288D1)
    val ButtonOkText = Color(0xFFFFFFFF)
    val ButtonCancelBg = Color(0xFFFFFFFF)
    val ButtonCancelText = Color(0xFF424242)
    val ButtonCancelBorder = Color(0xFFE0E0E0)
    val ButtonDangerBg = Color(0xFFFF5252)
    val ButtonDisabledBg = Color(0xFFEEEEEE)
    val ButtonDisabledText = Color(0xFF9E9E9E)
}

@Immutable
data class ColorApp(
    val colorBgLanguage: Color,
    val color5A5D5D: Color,
    val colorFFD755: Color,
    val colorWhite: Color,
    val colorBlack: Color,
    val colorBackgroundWindow: Color,
    val colorButtonOkBg: Color,
    val colorButtonOkText: Color,
    val colorButtonCancelBg: Color,
    val colorButtonCancelText: Color,
    val colorButtonCancelBorder: Color,
    val colorButtonDangerBg: Color,
    val colorButtonDisabledBg: Color,
    val colorButtonDisabledText: Color
)

internal val LocalAppColor = staticCompositionLocalOf { colorDefault }

val colorDefault =
    ColorApp(
        colorBgLanguage = ColorDefinition.BgLanguage,
        color5A5D5D = ColorDefinition.Color5A5D5D,
        colorFFD755 = ColorDefinition.ColorFFD755,
        colorWhite = ColorDefinition.White,
        colorBlack = ColorDefinition.Black,
        colorBackgroundWindow = ColorDefinition.Black,
        colorButtonOkBg = ColorDefinition.ButtonOkBg,
        colorButtonOkText = ColorDefinition.ButtonOkText,
        colorButtonCancelBg = ColorDefinition.ButtonCancelBg,
        colorButtonCancelText = ColorDefinition.ButtonCancelText,
        colorButtonCancelBorder = ColorDefinition.ButtonCancelBorder,
        colorButtonDangerBg = ColorDefinition.ButtonDangerBg,
        colorButtonDisabledBg = ColorDefinition.ButtonDisabledBg,
        colorButtonDisabledText = ColorDefinition.ButtonDisabledText
    )

val colorApprove =
    ColorApp(
        colorBgLanguage = ColorDefinition.BgLanguage,
        color5A5D5D = ColorDefinition.Color5A5D5D,
        colorFFD755 = ColorDefinition.ColorFFD755,
        colorWhite = ColorDefinition.White,
        colorBlack = ColorDefinition.Black,
        colorBackgroundWindow = ColorDefinition.Black,
        colorButtonOkBg = ColorDefinition.ButtonOkBg,
        colorButtonOkText = ColorDefinition.ButtonOkText,
        colorButtonCancelBg = ColorDefinition.ButtonCancelBg,
        colorButtonCancelText = ColorDefinition.ButtonCancelText,
        colorButtonCancelBorder = ColorDefinition.ButtonCancelBorder,
        colorButtonDangerBg = ColorDefinition.ButtonDangerBg,
        colorButtonDisabledBg = ColorDefinition.ButtonDisabledBg,
        colorButtonDisabledText = ColorDefinition.ButtonDisabledText
    )
