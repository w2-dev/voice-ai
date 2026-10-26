package com.tapbi.spark.voiceai.ui.main.lockoverlay.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig

@Composable
fun SwipeUnlockBar(
    themeConfig: OverlayThemeConfig,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "▲ Vuốt lên để mở khóa",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = themeConfig.accentColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onUnlock,
            colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accentColor),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "MỞ KHÓA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
