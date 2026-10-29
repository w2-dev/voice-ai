package com.tapbi.spark.voiceai.ui.main.lockoverlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.SwipeUnlockBar
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.UnlockMethodChip
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.VoiceUnlockContent
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig
import com.tapbi.spark.voiceai.utils.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

enum class OverlayUnlockMethod {
    VOICE,
    FINGERPRINT,
    PATTERN,
    PIN,
    SWIPE
}

@Composable
fun LockOverlayContent(
    onUnlockClick: () -> Unit,
    themeConfig: OverlayThemeConfig = OverlayThemeConfig()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var offsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY, label = "offsetY")

    // Default active method selection
    var selectedMethod by remember {
        mutableStateOf(
            when {
                LockScreenState.isPatternLockEnabled -> OverlayUnlockMethod.PATTERN
                LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup -> OverlayUnlockMethod.VOICE
                LockScreenState.isFingerprintEnabled -> OverlayUnlockMethod.FINGERPRINT
                LockScreenState.isPinLockEnabled && LockScreenState.hasPinSetup -> OverlayUnlockMethod.PIN
                else -> OverlayUnlockMethod.SWIPE
            }
        )
    }

    LaunchedEffect(Unit) {
        ServicePreferences.loadLockScreenStateSync(context)
    }

    LaunchedEffect(
        LockScreenState.isPatternLockEnabled,
        LockScreenState.targetPattern,
        LockScreenState.hasPatternSetup,
        LockScreenState.isVoiceLockEnabled,
        LockScreenState.voicePassphrase,
        LockScreenState.hasVoiceSetup,
        LockScreenState.isFingerprintEnabled,
        LockScreenState.isPinLockEnabled,
        LockScreenState.pinPassword,
        LockScreenState.hasPinSetup
    ) {
        selectedMethod = when {
            LockScreenState.isPatternLockEnabled && LockScreenState.hasPatternSetup -> OverlayUnlockMethod.PATTERN
            LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup -> OverlayUnlockMethod.VOICE
            LockScreenState.isFingerprintEnabled -> OverlayUnlockMethod.FINGERPRINT
            LockScreenState.isPinLockEnabled && LockScreenState.hasPinSetup -> OverlayUnlockMethod.PIN
            else -> OverlayUnlockMethod.SWIPE
        }
    }

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

    LaunchedEffect(Unit) {
        if (selectedMethod == OverlayUnlockMethod.FINGERPRINT) {
            triggerBiometricUnlock()
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
                            "Cosine Distance: ${"%.4f".format(result.distance)} (Ngưỡng < 0.45) | Similarity: ${
                                "%.1f".format(
                                    result.similarity * 100
                                )
                            }%"
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
                            "Cosine Distance: ${"%.4f".format(result.distance)} (Ngưỡng < 0.45) | Similarity: ${
                                "%.1f".format(
                                    result.similarity * 100
                                )
                            }%"
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

    val rootModifier = if (selectedMethod != OverlayUnlockMethod.PATTERN) {
        Modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -150f) {
                            onUnlockClick()
                        }
                        offsetY = 0f
                    },
                    onDragCancel = { offsetY = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount < 0 || offsetY < 0) {
                            offsetY += dragAmount
                        }
                    }
                )
            }
    } else {
        Modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
    }

    Box(
        modifier = rootModifier,
        contentAlignment = Alignment.Center
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
            // Top-End Security Question Button (?)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                    .clickable {
                        showSecurityOverlay = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            ) {
                // Live Clock Component
                OverlayClockHeader(themeConfig = themeConfig)

                Spacer(modifier = Modifier.height(24.dp))

                // Unlock Method Selector Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 20.dp)
                ) {
                    if (LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup) {
                        UnlockMethodChip(
                            icon = "🎤",
                            label = "Giọng nói",
                            isSelected = selectedMethod == OverlayUnlockMethod.VOICE,
                            onClick = {
                                if (!isRecording) {
                                    selectedMethod = OverlayUnlockMethod.VOICE
                                }
                            }
                        )
                    }

                    if (LockScreenState.isFingerprintEnabled) {
                        UnlockMethodChip(
                            icon = "👆",
                            label = "Vân tay",
                            isSelected = selectedMethod == OverlayUnlockMethod.FINGERPRINT,
                            onClick = {
                                if (!isRecording) {
                                    selectedMethod = OverlayUnlockMethod.FINGERPRINT
                                    triggerBiometricUnlock()
                                }
                            }
                        )
                    }

                    if (LockScreenState.isPinLockEnabled && LockScreenState.hasPinSetup) {
                        UnlockMethodChip(
                            icon = "🔢",
                            label = "Mã PIN",
                            isSelected = selectedMethod == OverlayUnlockMethod.PIN,
                            onClick = {
                                if (!isRecording) {
                                    selectedMethod = OverlayUnlockMethod.PIN
                                    pinInput = ""
                                    isPinError = false
                                }
                            }
                        )
                    }

                    if (LockScreenState.isPatternLockEnabled && LockScreenState.hasPatternSetup) {
                        UnlockMethodChip(
                            icon = "🎨",
                            label = "Hình vẽ",
                            isSelected = selectedMethod == OverlayUnlockMethod.PATTERN,
                            onClick = {
                                if (!isRecording) {
                                    selectedMethod = OverlayUnlockMethod.PATTERN
                                    isPatternError = false
                                }
                            }
                        )
                    }
                }

                // Display Active Method Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    this@Column.AnimatedVisibility(
                        visible = selectedMethod == OverlayUnlockMethod.VOICE,
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

                    this@Column.AnimatedVisibility(
                        visible = selectedMethod == OverlayUnlockMethod.FINGERPRINT,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FingerprintUnlockContent(
                            biometricStatus = biometricStatus,
                            themeConfig = themeConfig,
                            onFingerprintClick = { triggerBiometricUnlock() }
                        )
                    }

                    this@Column.AnimatedVisibility(
                        visible = selectedMethod == OverlayUnlockMethod.PIN,
                        enter = fadeIn(),
                        exit = fadeOut()
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
                    }

                    this@Column.AnimatedVisibility(
                        visible = selectedMethod == OverlayUnlockMethod.PATTERN,
                        enter = fadeIn(),
                        exit = fadeOut()
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
                    }

                    if (selectedMethod == OverlayUnlockMethod.SWIPE) {
                        Text(
                            text = "Vuốt lên hoặc chọn phương thức mở khóa ở trên",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Swipe / Unlock Bar Component
                SwipeUnlockBar(
                    themeConfig = themeConfig,
                    onUnlock = onUnlockClick
                )
            }
        }
    }
}
