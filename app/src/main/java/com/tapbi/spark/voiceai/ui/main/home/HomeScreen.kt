package com.tapbi.spark.voiceai.ui.main.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.ui.dialog.FingerprintSetupDialog
import com.tapbi.spark.voiceai.ui.dialog.PermissionDialog

/**
 * Màn hình HomeScreen.
 * Cấu trúc: HomeHeader -> HomeContent.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.ensureLockServiceStarted()
    }

    PermissionDialog(
        showDialog = uiState.showPermissionDialog,
        onDismissRequest = { viewModel.dismissPermissionDialog() },
        onAllPermissionsGranted = { viewModel.enableLockScreenFromDialog(context) }
    )

    FingerprintSetupDialog(
        showDialog = uiState.showFingerprintSetupDialog,
        onDismissRequest = { viewModel.dismissFingerprintSetupDialog() },
        onConfirmSetup = {
            viewModel.openSystemBiometricSettings(context)
            viewModel.dismissFingerprintSetupDialog()
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // 1. Header
        HomeHeader(
            onProClick = { viewModel.showPremiumScreen() },
            onSettingsClick = { viewModel.onSecurityQuestionSetupClick() },
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Content
        HomeContent(
            uiState = uiState,
            onActiveToggle = { viewModel.toggleLockScreenActive(context, !uiState.isLockScreenActive) },
            onVoiceToggle = { viewModel.toggleVoiceLock(it) },
            onPatternToggle = { viewModel.togglePatternLock(it) },
            onPinToggle = { viewModel.togglePinLock(it) },
            onFingerprintToggle = { viewModel.toggleFingerprint(context, it) },
            onVoiceSetup = { viewModel.onVoiceSetupClick() },
            onPinSetup = { viewModel.onPinSetupClick() },
            onPatternSetup = { viewModel.onPatternSetupClick() },
            onFingerprintSetup = { viewModel.onFingerprintClick(context) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

/**
 * Header chứa Tiêu đề "Home", Nút Pro 👑 và Nút Settings ⚙️ ở góc trên bên phải.
 */
@Composable
private fun HomeHeader(
    onProClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Home",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pro Crown Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF8E1))
                    .clickable(onClick = onProClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👑", fontSize = 18.sp)
            }

            // Settings Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE))
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚙️", fontSize = 18.sp)
            }
        }
    }
}

/**
 * Nội dung cuộn chính bao gồm Active Lock Shield, Lock Preview/Options Card, và Setup Lock Grid.
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onActiveToggle: () -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onPatternToggle: (Boolean) -> Unit,
    onPinToggle: (Boolean) -> Unit,
    onFingerprintToggle: (Boolean) -> Unit,
    onVoiceSetup: () -> Unit,
    onPinSetup: () -> Unit,
    onPatternSetup: () -> Unit,
    onFingerprintSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Active Lock Shield Card
        ActiveLockShieldCard(
            isActive = uiState.isLockScreenActive,
            onActiveToggle = onActiveToggle
        )

        // 2 & 3. Lock Screen Preview + Lock Options Card
        LockPreviewAndOptionsCard(
            uiState = uiState,
            onVoiceToggle = onVoiceToggle,
            onPatternToggle = onPatternToggle,
            onPinToggle = onPinToggle,
            onFingerprintToggle = onFingerprintToggle
        )

        // 4. Setup Lock Card Grid
        SetupLockCard(
            onVoiceSetup = onVoiceSetup,
            onPinSetup = onPinSetup,
            onPatternSetup = onPatternSetup,
            onFingerprintSetup = onFingerprintSetup
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Section 1: Active Lock Shield Card với nút Power Toggle
 */
@Composable
private fun ActiveLockShieldCard(
    isActive: Boolean,
    onActiveToggle: () -> Unit
) {
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Active Lock Shield",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Text(
                    text = if (isActive) "Active" else "Offline",
                    fontSize = 14.sp,
                    color = if (isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Medium
                )
            }

            // Circular Power Button Toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF4A6572) else Color(0xFFB0BEC5))
                    .clickable(onClick = onActiveToggle),
                contentAlignment = Alignment.Center
            ) {
                PowerIcon(
                    tint = if (isActive) Color.White else Color(0xFFE0E0E0),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Section 2 & 3: Thẻ kết hợp giữa Hình Xem Trước Màn Hình Khóa + Các Tùy Chọn Khóa
 */
@Composable
private fun LockPreviewAndOptionsCard(
    uiState: HomeUiState,
    onVoiceToggle: (Boolean) -> Unit,
    onPatternToggle: (Boolean) -> Unit,
    onPinToggle: (Boolean) -> Unit,
    onFingerprintToggle: (Boolean) -> Unit
) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Smartphone Screen Preview
            PhonePreviewWidget(
                modifier = Modifier
                    .width(135.dp)
                    .height(260.dp)
            )

            // Right: Lock Method Toggle Options
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Voice Lock
                LockMethodToggleRow(
                    iconBgColor = Color(0xFFE1F5FE),
                    icon = { WaveformIcon(modifier = Modifier.size(20.dp)) },
                    title = "Voice Lock",
                    checked = uiState.isVoiceLockEnabled,
                    enabled = uiState.isLockScreenActive,
                    onCheckedChange = onVoiceToggle
                )

                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                // 2. Pattern Lock
                LockMethodToggleRow(
                    iconBgColor = Color(0xFFE1F5FE),
                    icon = { PatternIcon(modifier = Modifier.size(20.dp)) },
                    title = "Pattern Lock",
                    checked = uiState.isPatternLockEnabled,
                    enabled = uiState.isLockScreenActive,
                    onCheckedChange = onPatternToggle
                )

                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                // 3. Pin Lock
                LockMethodToggleRow(
                    iconBgColor = Color(0xFFE1F5FE),
                    icon = { PinIcon(modifier = Modifier.size(20.dp)) },
                    title = "Pin Lock",
                    checked = uiState.isPinLockEnabled,
                    enabled = uiState.isLockScreenActive,
                    onCheckedChange = onPinToggle
                )

                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                // 4. Fingerprint
                LockMethodToggleRow(
                    iconBgColor = Color(0xFFFCE4EC),
                    icon = { FingerprintIcon(modifier = Modifier.size(20.dp)) },
                    title = "Fingerprint",
                    checked = uiState.isFingerprintEnabled,
                    enabled = uiState.isLockScreenActive,
                    onCheckedChange = onFingerprintToggle
                )
            }
        }
    }
}

/**
 * Dòng hiển thị loại khóa kèm nút bật/tắt (Switch)
 */
@Composable
private fun LockMethodToggleRow(
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }

            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF29B6F6),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.size(width = 40.dp, height = 24.dp)
            )
        }
    }
}

/**
 * Section 4: "Setup Lock" Card Grid
 */
@Composable
private fun SetupLockCard(
    onVoiceSetup: () -> Unit,
    onPinSetup: () -> Unit,
    onPatternSetup: () -> Unit,
    onFingerprintSetup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Setup Lock",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            // 2x2 Setup Item Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFFF0F5),
                        icon = { WaveformIcon(modifier = Modifier.size(20.dp)) },
                        title = "Voice setup",
                        onClick = onVoiceSetup
                    )

                    // Pin setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFFF0F5),
                        icon = { PinIcon(modifier = Modifier.size(20.dp)) },
                        title = "Pin setup",
                        onClick = onPinSetup
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pattern setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFE1F5FE),
                        icon = { PatternIcon(modifier = Modifier.size(20.dp)) },
                        title = "Pattern setup",
                        onClick = onPatternSetup
                    )

                    // Fingerprint
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFCE4EC),
                        icon = { FingerprintIcon(modifier = Modifier.size(20.dp)) },
                        title = "Fingerprint",
                        onClick = onFingerprintSetup
                    )
                }
            }
        }
    }
}

/**
 * Mỗi ô thiết lập loại khóa trong Setup Lock
 */
@Composable
private fun SetupItemBox(
    modifier: Modifier = Modifier,
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8F9FB))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )

                Text(
                    text = "›",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

/**
 * Widget mô phỏng màn hình khóa điện thoại (Phone Preview Widget)
 */
@Composable
private fun PhonePreviewWidget(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1), Color(0xFF01579B))
                )
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Clock and Date
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "08:32",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Wed, Aug 20\n☀️ 22°",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 12.sp
                )
            }

            // Bottom: Quote and Icons
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "\"Embrace your dreams,\nwhatever happens, protect you\"",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎙️", fontSize = 12.sp)
                    Text(text = "📷", fontSize = 12.sp)
                }
            }
        }
    }
}

// Custom Power Icon
@Composable
private fun PowerIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawArc(
            color = tint,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.7f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        drawLine(
            color = tint,
            start = Offset(w / 2f, h * 0.1f),
            end = Offset(w / 2f, h * 0.45f),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

// Vector Icon Composables
@Composable
private fun WaveformIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        val barWidth = 2.dp.toPx()
        val heights = listOf(0.4f, 0.8f, 1.0f, 0.6f, 0.9f, 0.5f)
        val startX = c.x - (heights.size * barWidth * 1.5f) / 2f

        heights.forEachIndexed { i, factor ->
            val x = startX + i * barWidth * 2f
            val barH = h * 0.6f * factor
            drawLine(
                color = Color(0xFF29B6F6),
                start = Offset(x, c.y - barH / 2f),
                end = Offset(x, c.y + barH / 2f),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
private fun PinIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dotR = 2.dp.toPx()
        val startX = w * 0.25f
        val gap = w * 0.25f
        val cy = h * 0.5f

        for (i in 0..2) {
            drawCircle(
                color = Color(0xFF29B6F6),
                radius = dotR,
                center = Offset(startX + i * gap, cy)
            )
        }
    }
}

@Composable
private fun PatternIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dotR = 1.8.dp.toPx()
        val offsets = listOf(
            Offset(w * 0.3f, h * 0.3f),
            Offset(w * 0.7f, h * 0.3f),
            Offset(w * 0.3f, h * 0.7f),
            Offset(w * 0.7f, h * 0.7f)
        )

        val path = Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            lineTo(offsets[1].x, offsets[1].y)
            lineTo(offsets[3].x, offsets[3].y)
            lineTo(offsets[2].x, offsets[2].y)
        }
        drawPath(path, color = Color(0xFF29B6F6), style = Stroke(width = 1.5.dp.toPx()))

        offsets.forEach {
            drawCircle(color = Color(0xFF29B6F6), radius = dotR, center = it)
        }
    }
}

@Composable
private fun FingerprintIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 1..3) {
            val arcSize = Size(w * 0.22f * i, h * 0.22f * i)
            drawArc(
                color = Color(0xFFEC407A),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(
                    w / 2f - arcSize.width / 2f,
                    h / 2f - arcSize.height / 2f + i * 1.5.dp.toPx()
                ),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
