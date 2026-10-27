package com.tapbi.spark.voiceai.ui.main.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.tapbi.spark.voiceai.ui.dialog.FingerprintSetupDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
            .background(Color(0xFFF7F9FC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card: Active Lock Screen and Options
        ActiveLockScreenCard(
            uiState = uiState,
            onActiveToggle = { viewModel.toggleLockScreenActive(context, it) },
            onVoiceToggle = { viewModel.toggleVoiceLock(it) },
            onPinToggle = { viewModel.togglePinLock(it) },
            onPatternToggle = { viewModel.togglePatternLock(it) },
            onFingerprintToggle = { viewModel.toggleFingerprint(context, it) },
            onSetupAiClick = { viewModel.onSetupAiProtectionClick() }
        )

        // Setup Action Cards
        SetupActionGrid(
            onVoiceSetup = { viewModel.onVoiceSetupClick() },
            onPinSetup = { viewModel.onPinSetupClick() },
            onPatternSetup = { viewModel.onPatternSetupClick() },
            onFingerprintSetup = { viewModel.onFingerprintClick(context) },
            onSecurityQuestionSetup = { viewModel.onSecurityQuestionSetupClick() }
        )

        // Bottom Banner: Lock Screen Themes
        LockScreenThemesBanner(
            onClick = { viewModel.onLockScreenThemesClick() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ActiveLockScreenCard(
    uiState: HomeUiState,
    onActiveToggle: (Boolean) -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onPinToggle: (Boolean) -> Unit,
    onPatternToggle: (Boolean) -> Unit,
    onFingerprintToggle: (Boolean) -> Unit,
    onSetupAiClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Active Lock Screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PhoneLockIcon(modifier = Modifier.size(36.dp))
                        Text(
                            text = "Active Lock Screen",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF29B6F6)
                        )
                    }

                    CustomSwitch(
                        checked = uiState.isLockScreenActive,
                        onCheckedChange = onActiveToggle
                    )
                }

                // Options List (Dimmed/Disabled if main lock screen is OFF)
                val optionsAlpha = if (uiState.isLockScreenActive) 1f else 0.3f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(optionsAlpha),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 1: Voice lock
                    LockOptionRow(
                        icon = { WaveformIcon(modifier = Modifier.size(32.dp)) },
                        title = "Voice lock",
                        checked = uiState.isVoiceLockEnabled,
                        enabled = uiState.isLockScreenActive,
                        onCheckedChange = onVoiceToggle
                    )

                    // Upgrade to AI Protection Banner
                    AiProtectionBanner(onSetupClick = onSetupAiClick)

                    // Item 2: Pin lock
                    LockOptionRow(
                        icon = { PinIcon(modifier = Modifier.size(32.dp)) },
                        title = "Pin lock",
                        checked = uiState.isPinLockEnabled,
                        enabled = uiState.isLockScreenActive,
                        onCheckedChange = onPinToggle
                    )

                    // Item 3: Pattern lock
                    LockOptionRow(
                        icon = { PatternIcon(modifier = Modifier.size(32.dp)) },
                        title = "Pattern lock",
                        checked = uiState.isPatternLockEnabled,
                        enabled = uiState.isLockScreenActive,
                        onCheckedChange = onPatternToggle
                    )

                    // Item 4: Fingerprint
                    LockOptionRow(
                        icon = { FingerprintIcon(modifier = Modifier.size(32.dp)) },
                        title = "Fingerprint",
                        checked = uiState.isFingerprintEnabled,
                        enabled = uiState.isLockScreenActive,
                        onCheckedChange = onFingerprintToggle
                    )
                }
            }

            // Overlay message when Lock Screen is OFF
            if (!uiState.isLockScreenActive) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lock screen is off\nturn on first",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LockOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFFBDBDBD)
            )
        }

        CustomSwitch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun AiProtectionBanner(
    onSetupClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF0F0))
            .border(1.dp, Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Red Exclamation Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = "Upgrade to AI Protection",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE57373)
                    )
                    Text(
                        text = "So only your voice can unlock.",
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD)
                    )
                }
            }

            Button(
                onClick = onSetupClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F7FA)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 4.dp
                )
            ) {
                Text(
                    text = "Setup",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF29B6F6)
                )
            }
        }
    }
}

@Composable
private fun SetupActionGrid(
    onVoiceSetup: () -> Unit,
    onPinSetup: () -> Unit,
    onPatternSetup: () -> Unit,
    onFingerprintSetup: () -> Unit,
    onSecurityQuestionSetup: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Voice setup
            ActionCard(
                modifier = Modifier.weight(1f),
                gradient = Brush.linearGradient(listOf(Color(0xFF81D4FA), Color(0xFF4FC3F7))),
                title = "Voice setup",
                subtitle = "(Use voice to open)",
                icon = { ActionCardLockIcon(iconType = "voice") },
                onClick = onVoiceSetup
            )

            // Pin setup
            ActionCard(
                modifier = Modifier.weight(1f),
                gradient = Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFFFD54F))),
                title = "Pin setup",
                subtitle = "(Lock with number pw)",
                icon = { ActionCardLockIcon(iconType = "pin") },
                onClick = onPinSetup
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pattern setup
            ActionCard(
                modifier = Modifier.weight(1f),
                gradient = Brush.linearGradient(listOf(Color(0xFFFFAB91), Color(0xFFFF8A65))),
                title = "Pattern setup",
                subtitle = "(Draw to open lock)",
                icon = { ActionCardLockIcon(iconType = "pattern") },
                onClick = onPatternSetup
            )

            // Fingerprint
            ActionCard(
                modifier = Modifier.weight(1f),
                gradient = Brush.linearGradient(listOf(Color(0xFFFFCC80), Color(0xFFFFB74D))),
                title = "Fingerprint",
                subtitle = "(Lock with fingerprint)",
                icon = { ActionCardLockIcon(iconType = "fingerprint") },
                onClick = onFingerprintSetup
            )
        }

        // Security Question Setup
        ActionCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = Brush.linearGradient(listOf(Color(0xFFB39DDB), Color(0xFF9575CD))),
            title = "Security Question",
            subtitle = "(Emergency unlock option)",
            icon = { Text(text = "❓", fontSize = 28.sp) },
            onClick = onSecurityQuestionSetup
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(vertical = 20.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon()

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD)
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF0277BD).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LockScreenThemesBanner(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0288D1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            PaletteWheelIcon(modifier = Modifier.size(36.dp))

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Lock Screen themes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFFFFB74D),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE0E0E0)
        )
    )
}

// Vector Icon Composables
@Composable
private fun PhoneLockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF29B6F6),
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.5.dp.toPx())
        )
        drawArc(
            color = Color(0xFF29B6F6),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.35f, h * 0.32f),
            size = Size(w * 0.3f, h * 0.25f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF29B6F6),
            topLeft = Offset(w * 0.32f, h * 0.45f),
            size = Size(w * 0.36f, h * 0.3f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

@Composable
private fun WaveformIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        val r = w / 2f
        drawCircle(color = Color(0xFFE1F5FE), radius = r)
        drawCircle(
            color = Color(0xFFB3E5FC),
            radius = r * 0.85f,
            style = Stroke(width = 1.dp.toPx())
        )

        val barWidth = 2.5.dp.toPx()
        val heights = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 0.4f)
        val startX = c.x - (heights.size * barWidth * 1.5f) / 2f

        heights.forEachIndexed { i, factor ->
            val x = startX + i * barWidth * 2.2f
            val barH = h * 0.4f * factor
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
        val r = w / 2f
        drawCircle(color = Color(0xFFFFF8E1), radius = r)
        drawCircle(
            color = Color(0xFFFFECB3),
            radius = r * 0.85f,
            style = Stroke(width = 1.dp.toPx())
        )

        val dotR = 2.5.dp.toPx()
        val startX = w * 0.3f
        val gap = w * 0.2f
        val cy = h * 0.5f

        for (i in 0..2) {
            drawCircle(
                color = Color(0xFFFFB74D),
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
        val r = w / 2f
        drawCircle(color = Color(0xFFFFEBEE), radius = r)
        drawCircle(
            color = Color(0xFFFFCDD2),
            radius = r * 0.85f,
            style = Stroke(width = 1.dp.toPx())
        )

        val dotR = 2.dp.toPx()
        val offsets = listOf(
            Offset(w * 0.35f, h * 0.35f),
            Offset(w * 0.65f, h * 0.35f),
            Offset(w * 0.35f, h * 0.65f),
            Offset(w * 0.65f, h * 0.65f)
        )

        val path = Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            lineTo(offsets[1].x, offsets[1].y)
            lineTo(offsets[3].x, offsets[3].y)
            lineTo(offsets[2].x, offsets[2].y)
        }
        drawPath(path, color = Color(0xFFE57373), style = Stroke(width = 1.5.dp.toPx()))

        offsets.forEach {
            drawCircle(color = Color(0xFFE57373), radius = dotR, center = it)
        }
    }
}

@Composable
private fun FingerprintIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w / 2f
        drawCircle(color = Color(0xFFFFF3E0), radius = r)
        drawCircle(
            color = Color(0xFFFFE0B2),
            radius = r * 0.85f,
            style = Stroke(width = 1.dp.toPx())
        )

        for (i in 1..3) {
            val arcSize = Size(w * 0.22f * i, h * 0.22f * i)
            drawArc(
                color = Color(0xFFFB8C00),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(
                    w / 2f - arcSize.width / 2f,
                    h / 2f - arcSize.height / 2f + i * 2.dp.toPx()
                ),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun ActionCardLockIcon(iconType: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        when (iconType) {
            "voice" -> WaveformIcon(modifier = Modifier.size(36.dp))
            "pin" -> PinIcon(modifier = Modifier.size(36.dp))
            "pattern" -> PatternIcon(modifier = Modifier.size(36.dp))
            "fingerprint" -> FingerprintIcon(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun PaletteWheelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w / 2f
        drawCircle(color = Color.White, radius = r)

        val colors =
            listOf(Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFF81D4FA), Color(0xFFAED581))
        val dotR = r * 0.22f
        val angles = listOf(0f, 90f, 180f, 270f)

        angles.forEachIndexed { i, angle ->
            val rad = Math.toRadians(angle.toDouble())
            val cx = (w / 2f + r * 0.45f * Math.cos(rad)).toFloat()
            val cy = (h / 2f + r * 0.45f * Math.sin(rad)).toFloat()
            drawCircle(color = colors[i], radius = dotR, center = Offset(cx, cy))
        }
    }
}
