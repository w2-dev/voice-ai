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
}

@Immutable
data class ColorApp(
    val colorBgLanguage: Color,
    val color5A5D5D: Color,
    val colorFFD755: Color,
    val colorWhite: Color,
    val colorBlack: Color,
    val colorBackgroundWindow: Color,
)

internal val LocalAppColor = staticCompositionLocalOf { colorDefault }

val colorDefault =
    ColorApp(
        colorBgLanguage = ColorDefinition.BgLanguage,
        color5A5D5D = ColorDefinition.Color5A5D5D,
        colorFFD755 = ColorDefinition.ColorFFD755,
        colorWhite = ColorDefinition.White,
        colorBlack = ColorDefinition.Black,
        colorBackgroundWindow = ColorDefinition.Black
    )

val colorApprove =
    ColorApp(
        colorBgLanguage = ColorDefinition.BgLanguage,
        color5A5D5D = ColorDefinition.Color5A5D5D,
        colorFFD755 = ColorDefinition.ColorFFD755,
        colorWhite = ColorDefinition.White,
        colorBlack = ColorDefinition.Black,
        colorBackgroundWindow = ColorDefinition.Black
    )