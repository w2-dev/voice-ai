package com.tapbi.spark.voiceai.ui.main.lockoverlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.voiceai.ui.main.pin.PinKeyPad

@Composable
fun PinUnlockContent(
    pinInput: String,
    isPinError: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isPinError) "Mã PIN không đúng! Thử lại" else "Nhập mã PIN để mở khóa",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPinError) Color(0xFFFF5252) else Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinInput.length
                val dotColor = when {
                    isPinError -> Color(0xFFFF5252)
                    isFilled -> Color(0xFF29B6F6)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = 1.5.dp,
                            color = if (isPinError) Color(0xFFFF5252) else Color.White.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                )
            }
        }

        PinKeyPad(
            buttonSize = 52.dp,
            spacing = 10.dp,
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            onClearClick = onClearClick
        )
    }
}
