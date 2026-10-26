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
fun FingerprintUnlockContent(
    biometricStatus: String,
    themeConfig: OverlayThemeConfig,
    onFingerprintClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(themeConfig.accentColor)
                .clickable(onClick = onFingerprintClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👆", fontSize = 40.sp)
        }

        Text(
            text = "Chạm biểu tượng quét Vân tay / Sinh trắc học",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )

        if (biometricStatus.isNotBlank()) {
            Text(
                text = biometricStatus,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (biometricStatus.contains("thành công", ignoreCase = true)) Color(0xFF81C784) else Color(0xFFFF8A65),
                textAlign = TextAlign.Center
            )
        }
    }
}
