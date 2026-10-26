package com.tapbi.spark.voiceai.ui.main.lockoverlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig

@Composable
fun VoiceUnlockContent(
    isRecording: Boolean,
    voiceStatus: String,
    recognizedTextInfo: String,
    vectorDistInfo: String,
    languageCode: String = "vi-VN",
    themeConfig: OverlayThemeConfig,
    onMicrophoneClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isRecording) Color(0xFFFF7043) else themeConfig.accentColor)
                .clickable(onClick = onMicrophoneClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎤", fontSize = 36.sp)
        }

        Text(
            text = if (isRecording) "Đang nghe ($languageCode)..." else "Chạm micro nói mật khẩu ($languageCode)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )

        if (voiceStatus.isNotBlank()) {
            Text(
                text = voiceStatus,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (voiceStatus.contains("THÀNH CÔNG")) Color(0xFF81C784) else Color(0xFFFF8A65),
                textAlign = TextAlign.Center
            )
        }

        if (recognizedTextInfo.isNotBlank()) {
            Text(
                text = recognizedTextInfo,
                fontSize = 13.sp,
                color = Color(0xFFE0F7FA),
                textAlign = TextAlign.Center
            )
        }

        if (vectorDistInfo.isNotBlank()) {
            Text(
                text = vectorDistInfo,
                fontSize = 12.sp,
                color = Color(0xFFFFF59D),
                textAlign = TextAlign.Center
            )
        }
    }
}
