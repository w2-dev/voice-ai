package com.tapbi.spark.voiceai.ui.main.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.ui.dialog.FingerprintSetupDialog
import com.tapbi.spark.voiceai.ui.dialog.PermissionDialog
import com.tapbi.spark.voiceai.ui.main.home.components.FingerprintIcon
import com.tapbi.spark.voiceai.ui.main.home.components.LockMethodToggleRow
import com.tapbi.spark.voiceai.ui.main.home.components.PatternIcon
import com.tapbi.spark.voiceai.ui.main.home.components.PhonePreviewWidget
import com.tapbi.spark.voiceai.ui.main.home.components.PinIcon
import com.tapbi.spark.voiceai.ui.main.home.components.PowerIcon
import com.tapbi.spark.voiceai.ui.main.home.components.SetupLockCard
import com.tapbi.spark.voiceai.ui.main.home.components.WaveformIcon

/**
 * Màn hình HomeScreen chuẩn theo hình ảnh thiết kế.
 * Cấu trúc: HomeHeader -> HomeContent (3 khối Card trắng tách biệt trên nền xám nhẹ).
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
        // 1. Header TopBar
        HomeHeader(
            onProClick = { viewModel.showPremiumScreen() },
            onSettingsClick = { viewModel.onSecurityQuestionSetupClick() },
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Content (3 khối Card trắng tách rời)
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
 * Nội dung cuộn chính chứa 3 khối Card trắng độc lập.
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
        // Card 1: Active Lock Shield Card
        ActiveLockShieldCard(
            isActive = uiState.isLockScreenActive,
            onActiveToggle = onActiveToggle
        )

        // Card 2: Lock Preview & Options Card
        LockPreviewAndOptionsCard(
            uiState = uiState,
            onVoiceToggle = onVoiceToggle,
            onPatternToggle = onPatternToggle,
            onPinToggle = onPinToggle,
            onFingerprintToggle = onFingerprintToggle
        )

        // Card 3: Setup Lock Card Grid
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
 * Card 1: Active Lock Shield Card với nút Power Toggle dạng Cyan Ring
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
                    text = if (isActive) "Running" else "Offline",
                    fontSize = 14.sp,
                    color = if (isActive) Color(0xFF616161) else Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Medium
                )
            }

            // Power Button với vòng viền Cyan
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F7FA))
                    .border(2.dp, if (isActive) Color(0xFF00B0FF) else Color(0xFFB0BEC5), CircleShape)
                    .clickable(onClick = onActiveToggle),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF00B0FF) else Color(0xFFB0BEC5)),
                    contentAlignment = Alignment.Center
                ) {
                    PowerIcon(
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card 2: Hình Xem Trước Màn Hình Khóa + Các Tùy Chọn Khóa Bật/Tắt
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
            // Left: Smartphone Screen Preview Widget
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
