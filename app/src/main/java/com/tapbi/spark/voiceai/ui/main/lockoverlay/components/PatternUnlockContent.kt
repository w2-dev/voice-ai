package com.tapbi.spark.voiceai.ui.main.lockoverlay.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.ui_common_sdk.components.ui.PatternLock

@Composable
fun PatternUnlockContent(
    isPatternError: Boolean,
    onPatternComplete: (List<Int>) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vẽ hình mẫu để mở khóa",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PatternLock(
            modifier = Modifier.size(220.dp),
            isError = isPatternError,
            clearTrigger = isPatternError,
            lineColor = Color(0xFF29B6F6),
            inactiveColor = Color.White.copy(alpha = 0.7f),
            errorColor = Color(0xFFFF5252),
            onPatternComplete = onPatternComplete
        )
    }
}
