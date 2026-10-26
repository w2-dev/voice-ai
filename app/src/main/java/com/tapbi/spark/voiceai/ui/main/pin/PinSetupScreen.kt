package com.tapbi.spark.voiceai.ui.main.pin

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.R

@Composable
fun PinSetupScreen(
    viewModel: PinSetupViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
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
                text = "Cài đặt mã PIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.openPremium() },
                contentAlignment = Alignment.Center
            ) {
                CrownIcon(modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tạo Mã PIN Mở Khóa",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (uiState.isError) Color(0xFFFF5252) else if (uiState.message.contains("✅")) Color(0xFF4CAF50) else Color(0xFF0288D1),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val length = uiState.currentInput.length
            val isError = uiState.isError
            for (i in 0 until 4) {
                val isFilled = i < length
                val dotColor = when {
                    isError -> Color(0xFFFF5252)
                    isFilled -> Color(0xFF0288D1)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = 2.dp,
                            color = if (isError) Color(0xFFFF5252) else Color(0xFF0288D1),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // KeyPad
        PinKeyPad(
            onDigitClick = { viewModel.onKeyPadClick(it) },
            onDeleteClick = { viewModel.onDeleteClick() },
            onClearClick = { viewModel.resetSetup() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetSetup() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Nhập Lại", color = Color(0xFF616161))
            }

            if (uiState.hasSavedPin) {
                Button(
                    onClick = { viewModel.deletePin() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Xóa Mã PIN", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PinKeyPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: androidx.compose.ui.unit.Dp = 60.dp,
    spacing: androidx.compose.ui.unit.Dp = 14.dp
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing * 1.4f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape)
                            .background(
                                when (key) {
                                    "C", "⌫" -> Color(0xFFEEEEEE)
                                    else -> Color(0xFFF5F5F5)
                                }
                            )
                            .clickable {
                                when (key) {
                                    "C" -> onClearClick()
                                    "⌫" -> onDeleteClick()
                                    else -> onDigitClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = (buttonSize.value * 0.35f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }
        }
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
        drawPath(path, color = Color(0xFFFFD54F))
    }
}
