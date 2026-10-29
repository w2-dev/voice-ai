package com.tapbi.spark.voiceai.ui.main.lockoverlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.core.content.ContextCompat
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.ServicePreferences
import com.tapbi.spark.voiceai.data.repository.VoiceRepository
import com.tapbi.spark.voiceai.data.repository.VoiceUnlockResult
import com.tapbi.spark.voiceai.extension.hasRecordAudioPermission
import com.tapbi.spark.voiceai.ui.main.biometric.BiometricActivity
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.FingerprintUnlockContent
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.OverlayClockHeader
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.PatternUnlockContent
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.PinUnlockContent
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.SecurityQuestionOverlayView
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.VoiceUnlockContent
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig
import com.tapbi.spark.voiceai.utils.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

enum class OverlayUnlockMethod {
    VOICE,
    PIN,
    FINGERPRINT,
    PATTERN,
    SWIPE
}

@Composable
fun LockOverlayContent(
    onUnlockClick: () -> Unit,
    themeConfig: OverlayThemeConfig = OverlayThemeConfig()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeInputOverlay by remember { mutableStateOf<OverlayUnlockMethod?>(null) }
    var voiceStatus by remember { mutableStateOf("") }
    var recognizedTextInfo by remember { mutableStateOf("") }
    var vectorDistInfo by remember { mutableStateOf("") }
    var biometricStatus by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isPatternError by remember { mutableStateOf(false) }
    var patternErrorCount by remember { mutableIntStateOf(0) }
    var pinInput by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var showSecurityOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ServicePreferences.loadLockScreenStateSync(context)
    }

    fun triggerBiometricUnlock() {
        biometricStatus = "Đang chờ xác thực vân tay / sinh trắc học..."
        try {
            val intent = Intent(context, BiometricActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error launching BiometricActivity")
            biometricStatus = "Lỗi mở giao diện vân tay: ${e.localizedMessage}"
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Constant.ACTION_BIOMETRIC_UNLOCK -> {
                        biometricStatus = "✅ Xác thực vân tay thành công! Mở khóa..."
                        onUnlockClick()
                    }

                    Constant.ACTION_SECURITY_QUESTION_UNLOCK -> {
                        onUnlockClick()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Constant.ACTION_BIOMETRIC_UNLOCK)
            addAction(Constant.ACTION_SECURITY_QUESTION_UNLOCK)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    LaunchedEffect(LockScreenState.speechLanguageCode) {
        coroutineScope.launch(Dispatchers.IO) {
            ModelManager.prewarmModels(context, LockScreenState.speechLanguageCode)
        }
    }

    LaunchedEffect(patternErrorCount) {
        if (patternErrorCount > 0) {
            isPatternError = true
            delay(1000L.milliseconds)
            isPatternError = false
        }
    }

    fun startVoiceRecognition() {
        if (context.hasRecordAudioPermission()) {
            if (isRecording) return
            isRecording = true
            activeInputOverlay = OverlayUnlockMethod.VOICE
            recognizedTextInfo = ""
            vectorDistInfo = ""

            coroutineScope.launch {
                val result = VoiceRepository.getInstance(context).recordAndVerifyVoiceUnlock(
                    context = context,
                    langCode = LockScreenState.speechLanguageCode,
                    targetPassphrase = LockScreenState.voicePassphrase,
                    referenceEmbedding = LockScreenState.referenceEmbedding,
                    onStatusUpdate = { status ->
                        voiceStatus = status
                    }
                )

                isRecording = false

                when (result) {
                    is VoiceUnlockResult.Success -> {
                        recognizedTextInfo = "ASR Text: \"${result.recognizedText}\""
                        vectorDistInfo = if (LockScreenState.referenceEmbedding != null) {
                            "Cosine Distance: ${"%.4f".format(result.distance)} | Similarity: ${"%.1f".format(result.similarity * 100)}%"
                        } else {
                            "Similarity: ${"%.1f".format(result.similarity * 100)}%"
                        }
                        voiceStatus = "✅ XÁC THỰC THÀNH CÔNG! Mở khóa..."
                        onUnlockClick()
                    }

                    is VoiceUnlockResult.Failure -> {
                        recognizedTextInfo =
                            if (result.recognizedText.isNotBlank()) "ASR Text: \"${result.recognizedText}\"" else "ASR Text: (Không nhận diện từ)"
                        vectorDistInfo = if (LockScreenState.referenceEmbedding != null) {
                            "Cosine Distance: ${"%.4f".format(result.distance)} | Similarity: ${"%.1f".format(result.similarity * 100)}%"
                        } else {
                            "Similarity: ${"%.1f".format(result.similarity * 100)}%"
                        }
                        voiceStatus = result.errorMessage
                    }
                }
            }
        } else {
            voiceStatus = "Vui lòng mở ứng dụng cấp quyền Micro"
        }
    }

    // Determine active unlock options
    val activeMethods = remember(
        LockScreenState.isVoiceLockEnabled,
        LockScreenState.hasVoiceSetup,
        LockScreenState.isPinLockEnabled,
        LockScreenState.hasPinSetup,
        LockScreenState.isFingerprintEnabled,
        LockScreenState.isPatternLockEnabled,
        LockScreenState.hasPatternSetup
    ) {
        val list = mutableListOf<OverlayUnlockMethod>()
        if (LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup) {
            list.add(OverlayUnlockMethod.VOICE)
        }
        if (LockScreenState.isPinLockEnabled && LockScreenState.hasPinSetup) {
            list.add(OverlayUnlockMethod.PIN)
        }
        if (LockScreenState.isFingerprintEnabled) {
            list.add(OverlayUnlockMethod.FINGERPRINT)
        }
        if (LockScreenState.isPatternLockEnabled && LockScreenState.hasPatternSetup) {
            list.add(OverlayUnlockMethod.PATTERN)
        }
        list
    }

    // Slot Allocation Priority: [Center, Start, End, RightEnd]
    val centerMethod: OverlayUnlockMethod? = remember(activeMethods) {
        when {
            activeMethods.contains(OverlayUnlockMethod.VOICE) -> OverlayUnlockMethod.VOICE
            activeMethods.isNotEmpty() -> activeMethods[0]
            else -> OverlayUnlockMethod.SWIPE
        }
    }

    val remainingMethods = remember(activeMethods, centerMethod) {
        activeMethods.filter { it != centerMethod }
    }

    val startMethod: OverlayUnlockMethod? = remainingMethods.getOrNull(0)
    val endMethod: OverlayUnlockMethod? = remainingMethods.getOrNull(1)
    val rightEndMethod: OverlayUnlockMethod? = remainingMethods.getOrNull(2)

    fun handleOptionClick(method: OverlayUnlockMethod) {
        when (method) {
            OverlayUnlockMethod.VOICE -> {
                startVoiceRecognition()
            }

            OverlayUnlockMethod.FINGERPRINT -> {
                triggerBiometricUnlock()
            }

            OverlayUnlockMethod.PIN -> {
                pinInput = ""
                isPinError = false
                activeInputOverlay = OverlayUnlockMethod.PIN
            }

            OverlayUnlockMethod.PATTERN -> {
                isPatternError = false
                activeInputOverlay = OverlayUnlockMethod.PATTERN
            }

            OverlayUnlockMethod.SWIPE -> {
                onUnlockClick()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3F51B5), Color(0xFF1A237E), Color(0xFF0D47A1))
                )
            )
    ) {
        if (showSecurityOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                SecurityQuestionOverlayView(
                    onBackClick = { showSecurityOverlay = false },
                    onSuccessUnlock = {
                        showSecurityOverlay = false
                        onUnlockClick()
                    }
                )
            }
        } else {
            // 1. Top-Right Security Question Button (7)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 20.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable { showSecurityOverlay = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 2. Top-Center Clock & Date Header
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OverlayClockHeader(themeConfig = themeConfig)
            }

            // 3. Active Input Overlay View (PIN / Pattern / Voice Status)
            if (activeInputOverlay != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = activeInputOverlay == OverlayUnlockMethod.PIN,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PinUnlockContent(
                                pinInput = pinInput,
                                isPinError = isPinError,
                                onDigitClick = { digit ->
                                    if (pinInput.length < 4) {
                                        val newPin = pinInput + digit
                                        pinInput = newPin
                                        isPinError = false
                                        if (newPin.length == 4) {
                                            coroutineScope.launch {
                                                delay(150L.milliseconds)
                                                if (newPin == LockScreenState.pinPassword) {
                                                    onUnlockClick()
                                                } else {
                                                    isPinError = true
                                                    pinInput = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                onDeleteClick = {
                                    if (pinInput.isNotEmpty()) {
                                        pinInput = pinInput.dropLast(1)
                                        isPinError = false
                                    }
                                },
                                onClearClick = {
                                    pinInput = ""
                                    isPinError = false
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "✕ Đóng",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { activeInputOverlay = null }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = activeInputOverlay == OverlayUnlockMethod.PATTERN,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PatternUnlockContent(
                                isPatternError = isPatternError,
                                clearTrigger = patternErrorCount,
                                onPatternComplete = { pattern ->
                                    val target = LockScreenState.targetPattern
                                    if (target.isNullOrEmpty() || pattern == target) {
                                        isPatternError = false
                                        onUnlockClick()
                                    } else {
                                        patternErrorCount++
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "✕ Đóng",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { activeInputOverlay = null }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = activeInputOverlay == OverlayUnlockMethod.VOICE,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        VoiceUnlockContent(
                            isRecording = isRecording,
                            voiceStatus = voiceStatus,
                            recognizedTextInfo = recognizedTextInfo,
                            vectorDistInfo = vectorDistInfo,
                            languageCode = LockScreenState.speechLanguageCode,
                            themeConfig = themeConfig,
                            onMicrophoneClick = { startVoiceRecognition() }
                        )
                    }
                }
            }

            // 4. Bottom Control Bar (Number 6)
            BottomUnlockControlBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                centerMethod = centerMethod,
                startMethod = startMethod,
                endMethod = endMethod,
                rightEndMethod = rightEndMethod,
                isRecording = isRecording,
                onMethodClick = { handleOptionClick(it) }
            )
        }
    }
}

/**
 * Thanh điều khiển mở khóa dưới đáy màn hình với 4 vị trí: [Start, Center, End, RightEnd]
 */
@Composable
private fun BottomUnlockControlBar(
    modifier: Modifier = Modifier,
    centerMethod: OverlayUnlockMethod?,
    startMethod: OverlayUnlockMethod?,
    endMethod: OverlayUnlockMethod?,
    rightEndMethod: OverlayUnlockMethod?,
    isRecording: Boolean,
    onMethodClick: (OverlayUnlockMethod) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Position 1: Start (Far Left)
            Box(
                modifier = Modifier.width(56.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (startMethod != null) {
                    SmallOptionPillButton(
                        method = startMethod,
                        onClick = { onMethodClick(startMethod) }
                    )
                }
            }

            // Position 2: Center (Primary Button)
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (centerMethod != null) {
                    CenterPrimaryButton(
                        method = centerMethod,
                        isRecording = isRecording,
                        onClick = { onMethodClick(centerMethod) }
                    )
                }
            }

            // Position 3 & 4: End & Right End (Far Right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (endMethod != null) {
                    SmallOptionCircleButton(
                        method = endMethod,
                        onClick = { onMethodClick(endMethod) }
                    )
                }

                if (rightEndMethod != null) {
                    SmallOptionPillButton(
                        method = rightEndMethod,
                        onClick = { onMethodClick(rightEndMethod) }
                    )
                }
            }
        }

        Text(
            text = "Tap to unlock",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Nút chính ở giữa (Center Primary Orange Circle)
 */
@Composable
private fun CenterPrimaryButton(
    method: OverlayUnlockMethod,
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(Color(0xFF3F4E5A).copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(if (isRecording) Color(0xFFE53935) else Color(0xFFF57C00))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when (method) {
                OverlayUnlockMethod.VOICE -> {
                    WhiteMicrophoneIcon(modifier = Modifier.size(30.dp))
                }

                OverlayUnlockMethod.PIN -> {
                    Text(text = "🔢", fontSize = 24.sp)
                }

                OverlayUnlockMethod.FINGERPRINT -> {
                    Text(text = "👆", fontSize = 24.sp)
                }

                OverlayUnlockMethod.PATTERN -> {
                    Text(text = "🎛️", fontSize = 24.sp)
                }

                OverlayUnlockMethod.SWIPE -> {
                    Text(text = "🔓", fontSize = 24.sp)
                }
            }
        }
    }
}

/**
 * Nút tùy chọn dạng Pill trắng ở góc (Start / RightEnd)
 */
@Composable
private fun SmallOptionPillButton(
    method: OverlayUnlockMethod,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        OptionIcon(method = method)
    }
}

/**
 * Nút tùy chọn dạng Tròn trắng (End)
 */
@Composable
private fun SmallOptionCircleButton(
    method: OverlayUnlockMethod,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        OptionIcon(method = method)
    }
}

@Composable
private fun OptionIcon(method: OverlayUnlockMethod) {
    when (method) {
        OverlayUnlockMethod.PIN -> OptionPinIcon(modifier = Modifier.size(20.dp))
        OverlayUnlockMethod.FINGERPRINT -> OptionFingerprintIcon(modifier = Modifier.size(20.dp))
        OverlayUnlockMethod.PATTERN -> OptionPatternIcon(modifier = Modifier.size(20.dp))
        OverlayUnlockMethod.VOICE -> Text(text = "🎙️", fontSize = 16.sp)
        OverlayUnlockMethod.SWIPE -> Text(text = "🔓", fontSize = 16.sp)
    }
}

// Icon Canvas Composables
@Composable
private fun WhiteMicrophoneIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.35f, h * 0.15f),
            size = Size(w * 0.3f, h * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )

        drawArc(
            color = Color.White,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.3f),
            size = Size(w * 0.56f, h * 0.45f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        drawLine(
            color = Color.White,
            start = Offset(w / 2f, h * 0.75f),
            end = Offset(w / 2f, h * 0.9f),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

@Composable
private fun OptionPinIcon(modifier: Modifier = Modifier) {
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
private fun OptionPatternIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dotR = 1.8.dp.toPx()
        val offsets = listOf(
            Offset(w * 0.3f, h * 0.3f),
            Offset(w * 0.7f, h * 0.3f),
            Offset(w * 0.3f, h * 0.65f),
            Offset(w * 0.7f, h * 0.65f)
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
private fun OptionFingerprintIcon(modifier: Modifier = Modifier) {
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
