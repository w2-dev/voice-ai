package com.tapbi.spark.voiceai.ui.main.lockoverlay

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.k2fsa.sherpa.onnx.SileroVadModelConfig
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractor
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractorConfig
import com.k2fsa.sherpa.onnx.Vad
import com.k2fsa.sherpa.onnx.VadModelConfig
import com.tapbi.spark.ui_common_sdk.components.ui.PatternLock
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.extension.hasRecordAudioPermission
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.OverlayClockHeader
import com.tapbi.spark.voiceai.ui.main.lockoverlay.components.SwipeUnlockBar
import com.tapbi.spark.voiceai.ui.main.lockoverlay.themes.OverlayThemeConfig
import com.tapbi.spark.voiceai.utils.ModelManager
import com.tapbi.spark.voiceai.utils.cosineDistance
import com.tapbi.spark.voiceai.utils.stringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt

enum class OverlayUnlockMethod {
    VOICE,
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

    // Default active method: VOICE if enabled, else SWIPE
    var selectedMethod by remember {
        mutableStateOf(
            when {
                LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup -> OverlayUnlockMethod.VOICE
                else -> OverlayUnlockMethod.SWIPE
            }
        )
    }

    var voiceStatus by remember { mutableStateOf("") }
    var recognizedTextInfo by remember { mutableStateOf("") }
    var vectorDistInfo by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isPatternError by remember { mutableStateOf(false) }

    fun startVoiceRecognition() {
        if (context.hasRecordAudioPermission()) {
            if (isRecording) return
            isRecording = true
            voiceStatus = "Đang lắng nghe giọng nói (4s)..."
            recognizedTextInfo = ""
            vectorDistInfo = ""

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val sampleRate = 16000
                    val channelConfig = AudioFormat.CHANNEL_IN_MONO
                    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                    val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val recorder = AudioRecord(
                            MediaRecorder.AudioSource.VOICE_RECOGNITION,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            maxOf(minBufferSize, 3200)
                        )

                        val audioData = mutableListOf<Float>()
                        val buffer = ByteArray(512 * 2)
                        recorder.startRecording()

                        val startTime = System.currentTimeMillis()
                        while (System.currentTimeMillis() - startTime < 4000) {
                            val readSize = recorder.read(buffer, 0, buffer.size)
                            if (readSize > 0) {
                                for (i in 0 until readSize step 2) {
                                    val s = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                                    audioData.add(s / 32768.0f)
                                }
                            }
                        }
                        recorder.stop()
                        recorder.release()

                        if (audioData.isEmpty()) {
                            withContext<Unit>(Dispatchers.Main) {
                                isRecording = false
                                voiceStatus = "Không ghi nhận được âm thanh! Thử lại."
                            }
                            return@launch
                        }

                        val rawAudioArray = audioData.toFloatArray()

                        // 1. Silero VAD check
                        var hasHumanSpeech = true
                        try {
                            val vadConfig = VadModelConfig(
                                sileroVadModelConfig = SileroVadModelConfig(
                                    model = "model/silero_vad_16k_op15.onnx",
                                    threshold = 0.3F,
                                    minSilenceDuration = 0.25F,
                                    minSpeechDuration = 0.15F,
                                    windowSize = 512
                                ),
                                sampleRate = 16000,
                                numThreads = 1,
                                provider = "cpu"
                            )
                            val vad = Vad(context.assets, vadConfig)
                            vad.acceptWaveform(rawAudioArray)
                            vad.flush()
                            hasHumanSpeech = vad.isSpeechDetected() || !vad.empty()
                            vad.release()
                        } catch (e: Throwable) {
                            Timber.w(e, "VAD check exception in overlay")
                        }

                        // 2. Zipformer ASR Text Match
                        var zipformerText = ""
                        try {
                            val recognizer = ModelManager.createRecognizer(context, LockScreenState.speechLanguageCode)
                            val offlineStream = recognizer.createStream()
                            offlineStream.acceptWaveform(rawAudioArray, 16000)
                            recognizer.decode(offlineStream)
                            val result = recognizer.getResult(offlineStream)
                            zipformerText = result.text.trim()
                            offlineStream.release()
                            recognizer.release()
                        } catch (e: Throwable) {
                            Timber.e(e, "Error Zipformer ASR on Overlay")
                        }

                        if (!hasHumanSpeech && zipformerText.isBlank()) {
                            withContext<Unit>(Dispatchers.Main) {
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
                                val config = SpeakerEmbeddingExtractorConfig(
                                    "model/3dspeaker_speech_campplus_sv_zh_en_16k-common_advanced.onnx",
                                    2,
                                    false,
                                    "cpu"
                                )
                                val extractor = SpeakerEmbeddingExtractor(context.assets, config)
                                val stream = extractor.createStream()
                                stream.acceptWaveform(rawAudioArray, 16000)
                                stream.inputFinished()
                                val testEmbedding = extractor.compute(stream)
                                stream.release()
                                extractor.release()

                                calculatedDist = cosineDistance(testEmbedding, LockScreenState.referenceEmbedding!!)
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
                            stringSimilarity(zipformerText.lowercase().trim(), targetKeyword.lowercase().trim())
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

                        withContext<Unit>(Dispatchers.Main) {
                            isRecording = false
                            recognizedTextInfo = if (zipformerText.isNotBlank()) "ASR Text: \"$zipformerText\"" else "ASR Text: (Không nhận diện từ)"
                            vectorDistInfo = if (LockScreenState.referenceEmbedding != null) {
                                "Cosine Distance: ${"%.4f".format(calculatedDist)} (Ngưỡng < 0.45) | Similarity: ${"%.1f".format(similarity * 100)}%"
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
                        withContext<Unit>(Dispatchers.Main) {
                            isRecording = false
                            voiceStatus = "Chưa cấp quyền RECORD_AUDIO!"
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in voice recognition on overlay")
                    withContext<Unit>(Dispatchers.Main) {
                        isRecording = false
                        voiceStatus = "Lỗi: ${e.localizedMessage}"
                    }
                }
            }
        } else {
            voiceStatus = "Vui lòng mở ứng dụng cấp quyền Micro"
        }
    }

    Box(
        modifier = Modifier
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
            },
        contentAlignment = Alignment.Center
    ) {
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                if (LockScreenState.isVoiceLockEnabled && LockScreenState.hasVoiceSetup) {
                    UnlockMethodChip(
                        icon = "🎤",
                        label = "Giọng nói",
                        isSelected = selectedMethod == OverlayUnlockMethod.VOICE,
                        onClick = { selectedMethod = OverlayUnlockMethod.VOICE }
                    )
                }

                if (LockScreenState.isPatternLockEnabled && LockScreenState.hasPatternSetup) {
                    UnlockMethodChip(
                        icon = "🎨",
                        label = "Hình vẽ",
                        isSelected = selectedMethod == OverlayUnlockMethod.PATTERN,
                        onClick = {
                            selectedMethod = OverlayUnlockMethod.PATTERN
                            isPatternError = false
                        }
                    )
                }
            }

            // Display Active Method Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                this@Column.AnimatedVisibility(
                    visible = selectedMethod == OverlayUnlockMethod.VOICE,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isRecording) Color(0xFFFF7043) else themeConfig.accentColor)
                                .clickable { startVoiceRecognition() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎤", fontSize = 36.sp)
                        }

                        Text(
                            text = if (isRecording) "Đang nghe..." else "Chạm micro nói mật khẩu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        if (voiceStatus.isNotBlank()) {
                            Text(
                                text = voiceStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (voiceStatus.contains("THÀNH CÔNG")) Color(0xFF81C784) else Color(
                                    0xFFFF8A65
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (recognizedTextInfo.isNotBlank()) {
                            Text(
                                text = recognizedTextInfo,
                                fontSize = 13.sp,
                                color = Color(0xFFE0F7FA),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (vectorDistInfo.isNotBlank()) {
                            Text(
                                text = vectorDistInfo,
                                fontSize = 12.sp,
                                color = Color(0xFFFFF59D),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = selectedMethod == OverlayUnlockMethod.PATTERN,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Vẽ hình mẫu để mở khóa",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        PatternLock(
                            modifier = Modifier.size(220.dp),
                            isError = isPatternError,
                            clearTrigger = isPatternError,
                            lineColor = Color(0xFF29B6F6),
                            inactiveColor = Color.White.copy(alpha = 0.7f),
                            errorColor = Color(0xFFFF5252),
                            onPatternComplete = { pattern ->
                                if (pattern == LockScreenState.targetPattern) {
                                    isPatternError = false
                                    onUnlockClick()
                                } else {
                                    isPatternError = true
                                }
                            }
                        )
                    }
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

            // Swipe / Unlock Bar Component (Always Available)
            SwipeUnlockBar(
                themeConfig = themeConfig,
                onUnlock = onUnlockClick
            )
        }
    }
}

@Composable
private fun UnlockMethodChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF29B6F6) else Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF29B6F6).copy(alpha = 0.35f) else Color.Black.copy(
                alpha = 0.4f
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF29B6F6) else Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
