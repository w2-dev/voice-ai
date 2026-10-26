package com.tapbi.spark.voiceai.ui.main.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import com.tapbi.spark.ui_common_sdk.components.ui.AnimateDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.k2fsa.sherpa.onnx.SileroVadModelConfig
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractor
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractorConfig
import com.k2fsa.sherpa.onnx.Vad
import com.k2fsa.sherpa.onnx.VadModelConfig
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.utils.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun PassphraseSetupScreen(
    viewModel: VoiceSetupViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun recordAndExtractEmbedding() {
        if (uiState.isRecording) return

        viewModel.setRecording(true)
        viewModel.updateStatusMessage("Đang ghi âm khẩu lệnh (tối đa 4s)...")

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
                                val s = ((buffer[i + 1].toInt() and 0xFF shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                                audioData.add(s / 32768.0f)
                            }
                        }
                    }
                    recorder.stop()
                    recorder.release()

                    val rawAudioArray = audioData.toFloatArray()

                    withContext<Unit>(Dispatchers.Main) {
                        viewModel.updateStatusMessage("Đang kiểm tra VAD & chạy Zipformer ASR...")
                    }

                    // Optional Silero VAD check
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
                        vad.release()
                    } catch (e: Throwable) {
                        Timber.w(e, "Silero VAD check exception")
                    }

                    withContext<Unit>(Dispatchers.Main) {
                        viewModel.updateStatusMessage("Đang nhận diện khẩu lệnh với Zipformer ASR...")
                    }

                    var zipformerText: String
                    try {
                        val recognizer = ModelManager.createRecognizer(context, uiState.languageCode)
                        val offlineStream = recognizer.createStream()
                        offlineStream.acceptWaveform(rawAudioArray, 16000)
                        recognizer.decode(offlineStream)
                        val result = recognizer.getResult(offlineStream)
                        zipformerText = result.text.trim()
                        if (zipformerText.isBlank()) {
                            zipformerText = "Không nhận diện được từ"
                        }

                        offlineStream.release()
                        recognizer.release()
                        Timber.d("Zipformer ASR text result: '$zipformerText'")
                    } catch (e: Throwable) {
                        Timber.e(e, "Error during Zipformer ASR execution")
                        zipformerText = "Lỗi Zipformer: ${e.localizedMessage}"
                    }

                    // Extract Speaker Embedding
                    var extractedEmbedding: FloatArray? = null
                    var vectorPreview = ""
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
                        extractedEmbedding = extractor.compute(stream)
                        stream.release()
                        extractor.release()

                        vectorPreview = "Vector [96d]: [${extractedEmbedding.take(4).joinToString(", ") { "%.2f".format(it) }}...]"
                        Timber.d("Speaker Embedding extracted successfully (size = ${extractedEmbedding.size})")
                    } catch (e: Throwable) {
                        Timber.e(e, "Error during SpeakerEmbeddingExtractor execution")
                        vectorPreview = "Lỗi trích xuất Vector: ${e.localizedMessage}"
                    }

                    val isSuccessRecognition = zipformerText.isNotBlank() &&
                            !zipformerText.contains("Không nhận diện", ignoreCase = true) &&
                            !zipformerText.contains("Lỗi", ignoreCase = true)

                    withContext<Unit>(Dispatchers.Main) {
                        viewModel.setRecording(false)

                        if (isSuccessRecognition) {
                            viewModel.processEnrollmentStep(
                                zipformerText,
                                extractedEmbedding,
                                vectorPreview
                            ) { isSuccess, msg, isFinished ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "❌ Lần ${uiState.enrollmentStep} thất bại! Không nghe rõ từ, vui lòng thử lại.",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.updateStatusMessage("❌ Lần ${uiState.enrollmentStep} không nhận diện được từ! Thử lại.")
                        }
                    }
                } else {
                    withContext<Unit>(Dispatchers.Main) {
                        viewModel.setRecording(false)
                        viewModel.updateStatusMessage("Chưa cấp quyền RECORD_AUDIO!")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during voice setup")
                withContext<Unit>(Dispatchers.Main) {
                    viewModel.setRecording(false)
                    viewModel.updateStatusMessage("❌ Lỗi: ${e.localizedMessage}")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recordAndExtractEmbedding()
        }
    }

    // Animated Dialog for Model Download Confirmation using AnimateDialog (DialogApp.kt)
    AnimateDialog(
        showDialog = uiState.showDownloadDialog,
        onDismissRequest = { viewModel.dismissDownloadDialog() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tải Mô Hình Giọng Nói",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Mô hình Zipformer cho ${uiState.selectedLanguage} chưa có trên máy. Bạn có muốn tải về ngay để sử dụng không?",
                    fontSize = 14.sp,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.dismissDownloadDialog() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Hủy", color = Color(0xFF616161))
                    }

                    Button(
                        onClick = {
                            viewModel.confirmDownloadModel(context) {
                                viewModel.updateStatusMessage("✅ Đã tải xong mô hình ${uiState.selectedLanguage}! Chạm nút Record để bắt đầu ghi âm.")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Tải Về", color = Color.White)
                    }
                }
            }
        }
    }

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
                text = "Passphrase setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )

            // VIP / Crown Icon
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

        Spacer(modifier = Modifier.height(20.dp))

        // Main Title
        Text(
            text = "Create Your Passphrase",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone & Person Speech Illustration
        PassphraseIllustration(modifier = Modifier.height(160.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitles
        Text(
            text = "Nói khẩu lệnh 3 lần để ứng dụng ghi nhận chính xác",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3-Step Enrollment Progress Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            for (step in 1..3) {
                val isDone = step < uiState.enrollmentStep
                val isCurrent = step == uiState.enrollmentStep
                val stepColor = when {
                    isDone -> Color(0xFF4CAF50)
                    isCurrent -> Color(0xFF0288D1)
                    else -> Color(0xFFE0E0E0)
                }
                val textColor = if (isDone || isCurrent) Color.White else Color(0xFF757575)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(stepColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDone) "✓" else "$step",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Text(
                        text = "Lần $step",
                        fontSize = 13.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) Color(0xFF0288D1) else Color(0xFF757575)
                    )
                }
            }
        }

        if (uiState.enrollmentStep > 1) {
            OutlinedButton(
                onClick = { viewModel.resetEnrollment() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Làm Lại Từ Lần 1", color = Color(0xFF0288D1))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Message
        if (uiState.statusMessage.isNotBlank()) {
            Text(
                text = uiState.statusMessage,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.statusMessage.contains("Thành công") || uiState.statusMessage.contains("Đã tải xong")) Color(0xFF2E7D32) else Color(0xFFFF5252),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Large Yellow Record Button
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(if (uiState.isRecording) Color(0xFFFF7043) else Color(0xFFFFD54F))
                .clickable {
                    if (viewModel.checkModelAndRecord(context)) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            MicIcon(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (uiState.isRecording) "Recording..." else "Tap to record",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF616161)
        )

        // Display Recorded Text and Vector Info Result Card
        if (uiState.recordedPassphrase.isNotBlank() || uiState.recordedVectorInfo.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Khẩu lệnh ASR nhận diện được:\n\"${uiState.recordedPassphrase.ifBlank { "Không nhận diện được từ" }}\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1),
                        textAlign = TextAlign.Center
                    )

                    if (uiState.recordedVectorInfo.isNotBlank()) {
                        Text(
                            text = uiState.recordedVectorInfo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Note
        Text(
            text = "Speak clearly so the system can recognize the command",
            fontSize = 13.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Language Selector Pill with Dropdown Menu
        var isDropdownExpanded by remember { mutableStateOf(false) }
        val supportedLanguages = listOf(
            "vi-VN" to "🇻🇳 Tiếng Việt (vi - int8)",
            "ru-RU" to "🇷🇺 Tiếng Nga (ru - int8)",
            "en-US" to "🇺🇸 Tiếng Anh (en - int8)",
            "zh-CN" to "🇨🇳 Tiếng Trung (zh - int8)",
            "ja-JP" to "🇯🇵 Tiếng Nhật (ja - int8)",
            "ko-KR" to "🇰🇷 Tiếng Hàn (ko - int8)",
            "th-TH" to "🇹🇭 Tiếng Thái (th - int8)",
            "yue-HK" to "🇭🇰 Tiếng Quảng Đông (yue - int8)"
        )

        Box {
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                    .clickable { isDropdownExpanded = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageMicIcon(modifier = Modifier.size(24.dp))
                        Text(
                            text = uiState.selectedLanguage,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF29B6F6)
                        )
                    }

                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = Color(0xFF29B6F6)
                    )
                }
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                supportedLanguages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            isDropdownExpanded = false
                            viewModel.selectLanguage(context, code, name)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = { viewModel.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Tiếp Tục", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Vector Illustration
@Composable
private fun PassphraseIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height

        // Phone
        val phoneW = w * 0.28f
        val phoneH = h * 0.75f
        val phoneX = w * 0.18f
        val phoneY = h * 0.15f

        drawRoundRect(
            color = Color(0xFF3F51B5),
            topLeft = Offset(phoneX, phoneY),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(12.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF81D4FA),
            topLeft = Offset(phoneX + 6.dp.toPx(), phoneY + 8.dp.toPx()),
            size = Size(phoneW - 12.dp.toPx(), phoneH - 24.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Red Sound Dot
        drawCircle(
            color = Color(0xFFFF5252),
            radius = 6.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Head/Person
        val personX = w * 0.62f
        val headR = h * 0.35f
        val headC = Offset(personX, h * 0.4f)

        // Hair/Head
        drawCircle(color = Color(0xFFFFB74D), radius = headR, center = headC)
        drawArc(
            color = Color(0xFFFFF3E0),
            startAngle = 40f,
            sweepAngle = 200f,
            useCenter = true,
            topLeft = Offset(headC.x - headR * 0.8f, headC.y - headR * 0.8f),
            size = Size(headR * 1.6f, headR * 1.6f)
        )

        // Mouth Curve
        drawArc(
            color = Color(0xFF5D4037),
            startAngle = 10f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(headC.x - 16.dp.toPx(), headC.y + 4.dp.toPx()),
            size = Size(24.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Shirt
        drawRoundRect(
            color = Color(0xFFFF5252),
            topLeft = Offset(personX - headR * 0.9f, h * 0.78f),
            size = Size(headR * 1.8f, h * 0.22f),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
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

@Composable
private fun MicIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(w * 0.38f, h * 0.15f),
            size = Size(w * 0.24f, h * 0.42f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawArc(
            color = Color(0xFF212121),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.28f, h * 0.25f),
            size = Size(w * 0.44f, h * 0.42f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawLine(
            color = Color(0xFF212121),
            start = Offset(w * 0.5f, h * 0.68f),
            end = Offset(w * 0.5f, h * 0.85f),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
private fun LanguageMicIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = Color(0xFF81D4FA),
            radius = w / 2f,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
