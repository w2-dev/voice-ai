package com.tapbi.spark.voiceai.ui.main.lockoverlay

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
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
import com.tapbi.spark.voiceai.utils.VoiceUtils
import com.tapbi.spark.voiceai.utils.cosineDistance
import com.tapbi.spark.voiceai.utils.stringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

            coroutineScope.launch(Dispatchers.IO) {
                // Ticker đếm ngược 4s -> 3s -> 2s -> 1s hiển thị thời gian thực
                val timerJob = coroutineScope.launch(Dispatchers.Main) {
                    var leftSec = 4
                    while (leftSec > 0 && isRecording) {
                        voiceStatus = "Đang lắng nghe giọng nói (${leftSec}s)..."
                        delay(1000L.milliseconds)
                        leftSec--
                    }
                }

                try {
                    val sampleRate = 16000
                    val channelConfig = AudioFormat.CHANNEL_IN_MONO
                    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                    val minBufferSize =
                        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val recorder = VoiceUtils.createAudioRecord(
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            minBufferSize
                        )

                        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
                            timerJob.cancel()
                            withContext(Dispatchers.Main) {
                                isRecording = false
                                voiceStatus = "❌ Micro bị bận hoặc không thể khởi tạo! Thử lại."
                            }
                            return@launch
                        }

                        val audioData = mutableListOf<Float>()
                        val buffer = ByteArray(512 * 2)

                        try {
                            recorder.startRecording()
                            val startTime = System.currentTimeMillis()
                            while (System.currentTimeMillis() - startTime < 4000) {
                                val readSize = recorder.read(buffer, 0, buffer.size)
                                if (readSize > 0) {
                                    for (i in 0 until readSize step 2) {
                                        val s =
                                            ((buffer[i + 1].toInt() and 0xFF shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                                        audioData.add(s / 32768.0f)
                                    }
                                }
                            }
                        } finally {
                            try {
                                recorder.stop()
                            } catch (e: Exception) {
                                Timber.w(e)
                            }
                            try {
                                recorder.release()
                            } catch (e: Exception) {
                                Timber.w(e)
                            }
                            timerJob.cancel()
                        }

                        if (audioData.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                isRecording = false
                                voiceStatus = "Không ghi nhận được âm thanh! Thử lại."
                            }
                            return@launch
                        }

                        val rawAudioArray = audioData.toFloatArray()

                        // 1. Silero VAD check
                        var hasHumanSpeech = true
                        try {
                            val vad = ModelManager.getOrNewVad(context)
                            vad.acceptWaveform(rawAudioArray)
                            vad.flush()
                            hasHumanSpeech = vad.isSpeechDetected() || !vad.empty()
                        } catch (e: Throwable) {
                            Timber.w(e, "VAD check exception in overlay")
                        }

                        // 2. Zipformer ASR Text Match
                        val activeLangCode = LockScreenState.speechLanguageCode
                        if (!ModelManager.isModelDownloaded(context, activeLangCode)) {
                            withContext(Dispatchers.Main) {
                                isRecording = false
                                voiceStatus = "⚠️ Vui lòng mở App tải mô hình cho $activeLangCode!"
                            }
                            return@launch
                        }

                        var zipformerText = ""
                        try {
                            val recognizer =
                                ModelManager.getOrNewRecognizer(context, activeLangCode)
                            val offlineStream = recognizer.createStream()
                            offlineStream.acceptWaveform(rawAudioArray, 16000)
                            recognizer.decode(offlineStream)
                            val result = recognizer.getResult(offlineStream)
                            zipformerText = result.text.trim()
                            offlineStream.release()
                        } catch (e: Throwable) {
                            Timber.e(e, "Error Zipformer ASR on Overlay")
                        }

                        if (!hasHumanSpeech && zipformerText.isBlank()) {
                            withContext(Dispatchers.Main) {
                                isRecording = false
                                voiceStatus = "Không nghe thấy giọng nói! Thử lại."
                            }
                            return@launch
                        }

                        // 3. Speaker Embedding Verification
                        var isSpeakerMatched = false
                        var calculatedDist = 1.0f
                        if (LockScreenState.referenceEmbedding != null) {
                            try {
                                val extractor = ModelManager.getOrNewExtractor(context)
                                val stream = extractor.createStream()
                                stream.acceptWaveform(rawAudioArray, 16000)
                                stream.inputFinished()
                                val testEmbedding = extractor.compute(stream)
                                stream.release()

                                calculatedDist = cosineDistance(
                                    testEmbedding,
                                    LockScreenState.referenceEmbedding!!
                                )
                                if (calculatedDist < 0.45f) {
                                    isSpeakerMatched = true
                                }
                            } catch (e: Throwable) {
                                Timber.e(e, "Error SpeakerEmbeddingExtractor on Overlay")
                            }
                        } else {
                            isSpeakerMatched = true
                        }

                        // Check Keyword Similarity
                        val targetKeyword = LockScreenState.voicePassphrase
                        val similarity = if (targetKeyword.isNotBlank()) {
                            stringSimilarity(
                                zipformerText.lowercase().trim(),
                                targetKeyword.lowercase().trim()
                            )
                        } else {
                            0.0
                        }

                        val isTextMatched = targetKeyword.isBlank() || similarity >= 0.3 ||
                                zipformerText.contains(targetKeyword, ignoreCase = true) ||
                                targetKeyword.contains(zipformerText, ignoreCase = true)

                        val isUnlockSuccessful = if (LockScreenState.referenceEmbedding != null) {
                            isSpeakerMatched && isTextMatched
                        } else {
                            isTextMatched
                        }

                        withContext(Dispatchers.Main) {
                            isRecording = false
                            recognizedTextInfo =
                                if (zipformerText.isNotBlank()) "ASR Text: \"$zipformerText\"" else "ASR Text: (Không nhận diện từ)"
                            vectorDistInfo = if (LockScreenState.referenceEmbedding != null) {
                                "Cosine Distance: ${"%.4f".format(calculatedDist)} (Ngưỡng < 0.45) | Similarity: ${
                                    "%.1f".format(
                                        similarity * 100
                                    )
                                }%"
                            } else {
                                "Similarity: ${"%.1f".format(similarity * 100)}%"
                            }

                            if (isUnlockSuccessful) {
                                voiceStatus = "✅ XÁC THỰC THÀNH CÔNG! Mở khóa..."
                                onUnlockClick()
                            } else {
                                voiceStatus = when {
                                    !isTextMatched && !isSpeakerMatched -> "❌ Khai báo sai khẩu lệnh và không đúng giọng nói!"
                                    !isTextMatched -> "❌ Sai khẩu lệnh từ khóa!"
                                    else -> "❌ Khẩu lệnh đúng nhưng KHÁC giọng người đăng ký!"
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isRecording = false
                            voiceStatus = "Chưa cấp quyền RECORD_AUDIO!"
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in voice recognition on overlay")
                    withContext(Dispatchers.Main) {
                        isRecording = false
                        voiceStatus = "Lỗi: ${e.localizedMessage}"
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

                    if (LockScreenState.isPatternLockEnabled) {
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
