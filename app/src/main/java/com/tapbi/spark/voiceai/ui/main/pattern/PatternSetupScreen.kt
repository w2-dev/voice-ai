package com.tapbi.spark.voiceai.ui.main.pattern

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonApp
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonAppStyle
import com.tapbi.spark.ui_common_sdk.components.ui.PatternLock
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.ui.main.pattern.components.PatternStepperHeader

@Composable
fun PatternSetupScreen(
    viewModel: PatternSetupViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isStepFinished = uiState.step == PatternSetupStep.SUCCESS

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.popBackStack() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Pattern Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            // Right Checkmark or Crown Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isStepFinished) {
                            viewModel.navigateToHome()
                        } else {
                            viewModel.openPremium()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isStepFinished) {
                    Text(
                        text = "✓",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    CrownIcon(modifier = Modifier.size(28.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stepper
        PatternStepperHeader(currentStep = uiState.step)

        Spacer(modifier = Modifier.height(16.dp))

        // Step Title
        val stepTitle = when (uiState.step) {
            PatternSetupStep.FIRST_DRAW -> buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)) {
                    append("Step 2: ")
                }
                withStyle(SpanStyle(color = Color(0xFF212121), fontWeight = FontWeight.Bold)) {
                    append("Draw your pattern")
                }
            }
            PatternSetupStep.CONFIRM_DRAW -> buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)) {
                    append("Step 3: ")
                }
                withStyle(SpanStyle(color = Color(0xFF212121), fontWeight = FontWeight.Bold)) {
                    append("Confirm your pattern")
                }
            }
            else -> buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)) {
                    append("Complete: ")
                }
                withStyle(SpanStyle(color = Color(0xFF212121), fontWeight = FontWeight.Bold)) {
                    append("Pattern Saved!")
                }
            }
        }

        Text(
            text = stepTitle,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Message
        Text(
            text = uiState.message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (uiState.isError) Color(0xFFFF5252) else Color(0xFF0288D1),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hide Pattern Canvas on Success
        if (uiState.step != PatternSetupStep.SUCCESS) {
            PatternLock(
                modifier = Modifier
                    .size(280.dp)
                    .padding(12.dp),
                rows = uiState.selectedRows,
                cols = uiState.selectedCols,
                isError = uiState.isError,
                clearTrigger = Triple(uiState.step, uiState.message, uiState.errorCount),
                onPatternComplete = { pattern ->
                    viewModel.onPatternDrawn(pattern)
                }
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎉 Mật Khẩu Đã Được Lưu!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Nhấn dấu tích (✓) ở góc trên để hoàn tất.",
                        fontSize = 14.sp,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ButtonApp(
                text = "Vẽ Lại",
                onClick = { viewModel.resetSetup() },
                style = ButtonAppStyle.CANCEL,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )

            if (isStepFinished) {
                ButtonApp(
                    text = "Xóa Mật Khẩu",
                    onClick = { viewModel.deletePattern() },
                    style = ButtonAppStyle.DANGER,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CrownIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.8f)
            lineTo(w * 0.2f, h * 0.3f)
            lineTo(w * 0.4f, h * 0.6f)
            lineTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.6f, h * 0.6f)
            lineTo(w * 0.8f, h * 0.3f)
            lineTo(w * 0.9f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = Color(0xFFFFD54F))
    }
}
