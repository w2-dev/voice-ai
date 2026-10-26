package com.tapbi.spark.voiceai.ui.main.lockoverlay.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.models.LockScreenState

@Composable
fun SecurityQuestionOverlayView(
    onBackClick: () -> Unit,
    onSuccessUnlock: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(LockScreenState.securityQuestionIndex) }
    var answerInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Nhập câu trả lời để xác thực mở khóa") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val questions = LockScreenState.SAMPLE_SECURITY_QUESTIONS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Câu hỏi bảo mật",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mở Khóa Bằng Câu Hỏi Bảo Mật",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (message.isNotBlank()) {
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isError -> Color(0xFFFF5252)
                    isSuccess -> Color(0xFF4CAF50)
                    else -> Color(0xFF0288D1)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chọn câu hỏi bảo mật:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .border(1.5.dp, Color(0xFF0288D1), RoundedCornerShape(12.dp))
                .clickable { isDropdownExpanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = questions.getOrElse(selectedIndex) { questions[0] },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▼",
                    fontSize = 12.sp,
                    color = Color(0xFF0288D1)
                )
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(0.88f)
            ) {
                questions.forEachIndexed { index, question ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = question,
                                fontSize = 14.sp,
                                fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == selectedIndex) Color(0xFF0288D1) else Color(0xFF212121)
                            )
                        },
                        onClick = {
                            selectedIndex = index
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Câu trả lời của bạn:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = answerInput,
            onValueChange = {
                answerInput = it
                isError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            placeholder = {
                Text(
                    text = "Nhập câu trả lời tại đây...",
                    color = Color(0xFF9E9E9E),
                    fontSize = 15.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedBorderColor = Color(0xFF0288D1),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color(0xFF212121),
                unfocusedTextColor = Color(0xFF212121)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val trimmedInput = answerInput.trim()
                if (trimmedInput.isBlank()) {
                    message = "Vui lòng nhập câu trả lời!"
                    isError = true
                    return@Button
                }

                if (LockScreenState.hasSecurityQuestionSetup) {
                    if (trimmedInput.equals(LockScreenState.securityAnswer, ignoreCase = true)) {
                        message = "✅ Xác thực thành công! Đang mở khóa..."
                        isError = false
                        isSuccess = true
                        onSuccessUnlock()
                    } else {
                        message = "❌ Câu trả lời không đúng! Vui lòng thử lại."
                        isError = true
                    }
                } else {
                    LockScreenState.securityQuestionIndex = selectedIndex
                    LockScreenState.securityAnswer = trimmedInput
                    message = "✅ Đã lưu câu hỏi & mở khóa thành công!"
                    isError = false
                    isSuccess = true
                    onSuccessUnlock()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = "Xác Nhận Mở Khóa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
