package com.tapbi.spark.voiceai.ui.main.voice

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.R

@Composable
fun VoiceSetupScreen(
    viewModel: VoiceSetupViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshState()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFC))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.popBackStack() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Voice setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val subText = if (uiState.recordedPassphrase.isNotBlank()) {
                "Khẩu lệnh: \"${uiState.recordedPassphrase}\"\n${if (uiState.recordedVectorInfo.isNotBlank()) uiState.recordedVectorInfo else "Đã lưu Voice Vector 96d"}"
            } else {
                "Chưa cài đặt mật khẩu giọng nói"
            }

            // Card 1: Setup or change password Voice lock (Featured Blue Border)
            CardOptionRow(
                icon = { VoiceLockCircleIcon(modifier = Modifier.size(48.dp)) },
                title = "Setup or change password\nVoice lock",
                subtitle = subText,
                borderColor = Color(0xFF81D4FA),
                backgroundColor = Color.White,
                onClick = { viewModel.openPassphraseSetup() }
            )

            // Card 2: Voice lock manager
            CardOptionRow(
                icon = { FolderHierarchyIcon(modifier = Modifier.size(48.dp)) },
                title = "Voice lock manager",
                backgroundColor = Color.White,
                onClick = { }
            )

            // Card 3: Change recover password by question
            CardOptionRow(
                icon = { QuestionBubbleIcon(modifier = Modifier.size(48.dp)) },
                title = "Change recover password by\nquestion",
                backgroundColor = Color(0xFFE0E0E0),
                onClick = { }
            )

            // Card 4: Change recover password by email
            CardOptionRow(
                icon = { EmailShieldIcon(modifier = Modifier.size(48.dp)) },
                title = "Change recover password by\nemail",
                backgroundColor = Color(0xFFE0E0E0),
                onClick = { }
            )

            // Card 5: Auto listen the password
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoListenEarIcon(modifier = Modifier.size(48.dp))
                        Text(
                            text = "Auto listen the\npassword",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF616161)
                        )
                    }

                    Switch(
                        checked = uiState.isAutoListenEnabled,
                        onCheckedChange = { viewModel.toggleAutoListen(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4FC3F7),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CardOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    backgroundColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    var cardModifier = Modifier
        .fillMaxWidth()
        .shadow(2.dp, shape)
        .clip(shape)
        .clickable(onClick = onClick)

    if (borderColor != null) {
        cardModifier = cardModifier.border(2.dp, borderColor, shape)
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon()
            Column {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF616161),
                    lineHeight = 22.sp
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Helper Vector Icons
@Composable
private fun VoiceLockCircleIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFE1F5FE)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2f, h / 2f)
            val barW = 2.5.dp.toPx()
            val heights = listOf(0.4f, 0.8f, 1f, 0.6f, 0.3f)
            val startX = c.x - (heights.size * barW * 1.5f) / 2f

            heights.forEachIndexed { i, factor ->
                val x = startX + i * barW * 2f
                val barH = h * 0.5f * factor
                drawLine(
                    color = Color(0xFF29B6F6),
                    start = Offset(x, c.y - barH / 2f),
                    end = Offset(x, c.y + barH / 2f),
                    strokeWidth = barW
                )
            }
        }
    }
}

@Composable
private fun FolderHierarchyIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(w * 0.3f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.3f)
        )
        drawRect(
            color = Color(0xFFFFE082),
            topLeft = Offset(w * 0.1f, h * 0.6f),
            size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.22f)
        )
        drawRect(
            color = Color(0xFFFFE082),
            topLeft = Offset(w * 0.39f, h * 0.6f),
            size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.22f)
        )
        drawRect(
            color = Color(0xFFFFE082),
            topLeft = Offset(w * 0.68f, h * 0.6f),
            size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.22f)
        )
    }
}

@Composable
private fun QuestionBubbleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFFFFA726), center = Offset(w / 2f, h / 2f), radius = w * 0.4f)
    }
}

@Composable
private fun EmailShieldIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            color = Color(0xFFFFB74D),
            topLeft = Offset(w * 0.15f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.5f)
        )
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.25f)
            lineTo(w * 0.5f, h * 0.55f)
            lineTo(w * 0.85f, h * 0.25f)
        }
        drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun AutoListenEarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF29B6F6),
            topLeft = Offset(w * 0.25f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.7f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
