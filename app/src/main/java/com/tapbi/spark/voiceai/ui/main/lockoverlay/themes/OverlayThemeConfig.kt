package com.tapbi.spark.voiceai.ui.main.lockoverlay.themes

import androidx.compose.ui.graphics.Color

data class OverlayThemeConfig(
    val id: String = "default",
    val name: String = "Default Theme",
    val backgroundColor: Color = Color(0xD9000000),
    val primaryTextColor: Color = Color.White,
    val accentColor: Color = Color(0xFF29B6F6),
    val clockStyle: ClockStyle = ClockStyle.DIGITAL
)

enum class ClockStyle {
    DIGITAL,
    MINIMAL,
    NEON
}
