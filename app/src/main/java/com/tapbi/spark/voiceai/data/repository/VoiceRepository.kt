package com.tapbi.spark.voiceai.data.repository

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.common.models.EnrollmentResult
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.data.preferences.ServicePreferences
import com.tapbi.spark.voiceai.extension.prefsDatastore
import com.tapbi.spark.voiceai.utils.ModelManager
import com.tapbi.spark.voiceai.utils.VoiceUtils
import com.tapbi.spark.voiceai.utils.cosineDistance
import com.tapbi.spark.voiceai.utils.stringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

sealed class VoiceUnlockResult {
    data class Success(
        val recognizedText: String,
        val distance: Float,
        val similarity: Double
    ) : VoiceUnlockResult()

    data class Failure(
        val recognizedText: String,
        val distance: Float,
        val similarity: Double,
        val errorMessage: String
    ) : VoiceUnlockResult()
}

@Singleton
class VoiceRepository @Inject constructor(
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) {
    init {
        INSTANCE = this
    }

    companion object {
        @Volatile
        private var INSTANCE: VoiceRepository? = null

        fun getInstance(context: Context): VoiceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceRepository(
                    PreferencesDataStoreHelper(context.applicationContext.prefsDatastore)
                ).also { INSTANCE = it }
            }
        }

        val instance: VoiceRepository
            get() = INSTANCE ?: error("VoiceRepository instance not initialized")
    }

    private val recordedEmbeddings = mutableListOf<FloatArray>()
    var currentEnrollmentStep: Int = 1
        private set
    var firstPassphrase: String = ""
        private set

    fun resetEnrollment() {
        recordedEmbeddings.clear()
        currentEnrollmentStep = 1
        firstPassphrase = ""
    }

    suspend fun recordAndEnrollVoiceStep(
        context: Context,
        languageCode: String,
        onStatusUpdate: (String) -> Unit
    ): EnrollmentResult = withContext(Dispatchers.IO) {
        onStatusUpdate("Đang ghi âm (4s)...")

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val recorder = VoiceUtils.createAudioRecord(
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        ) ?: return@withContext EnrollmentResult.MismatchError(
            currentEnrollmentStep,
            "",
            "❌ Micro bận hoặc không thể khởi tạo! Thử lại."
        )

        val audioData = mutableListOf<Float>()
        val buffer = ByteArray(512 * 2)

        val timerJob = launch {
            var leftSec = 4
            while (leftSec > 0) {
                withContext(Dispatchers.Main) {
                    onStatusUpdate("Đang ghi âm (${leftSec}s)...")
                }
                delay(1000L.milliseconds)
                leftSec--
            }
        }

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
            timerJob.cancel()
            try {
                recorder.stop()
            } catch (_: Exception) {
            }
            try {
                recorder.release()
            } catch (_: Exception) {
            }
        }

        val rawAudioArray = audioData.toFloatArray()
        val rms = VoiceUtils.calculateRms(rawAudioArray)
        if (rms < 0.008f) {
            return@withContext EnrollmentResult.MismatchError(
                currentEnrollmentStep,
                "",
                "❌ Im lặng! Vui lòng nói khẩu lệnh rõ ràng."
            )
        }

        onStatusUpdate("Đang nhận diện giọng nói...")

        var hasHumanSpeech = true
        try {
            val vad = ModelManager.getOrNewVad(context)
            vad.acceptWaveform(rawAudioArray)
            vad.flush()
            hasHumanSpeech = vad.isSpeechDetected() || !vad.empty()
        } catch (e: Throwable) {
            Timber.w(e, "Silero VAD check exception")
        }

        if (!hasHumanSpeech) {
            return@withContext EnrollmentResult.MismatchError(
                currentEnrollmentStep,
                "",
                "❌ Không nghe thấy tiếng người! Thử lại."
            )
        }

        var rawZipformerText = ""
        try {
            val recognizer = ModelManager.getOrNewRecognizer(context, languageCode)
            val offlineStream = recognizer.createStream()
            offlineStream.acceptWaveform(rawAudioArray, 16000)
            recognizer.decode(offlineStream)
            val result = recognizer.getResult(offlineStream)
            rawZipformerText = result.text.trim()
            offlineStream.release()
        } catch (e: Throwable) {
            Timber.e(e, "Error Zipformer ASR")
        }

        val zipformerText = VoiceUtils.cleanPassphraseText(rawZipformerText)
        if (zipformerText.length < 2) {
            return@withContext EnrollmentResult.MismatchError(
                currentEnrollmentStep,
                "",
                "❌ Không nhận diện được từ khóa khẩu lệnh!"
            )
        }

        var extractedEmbedding: FloatArray? = null
        try {
            val extractor = ModelManager.getOrNewExtractor(context)
            val stream = extractor.createStream()
            stream.acceptWaveform(rawAudioArray, 16000)
            stream.inputFinished()
            extractedEmbedding = extractor.compute(stream)
            stream.release()
        } catch (e: Throwable) {
            Timber.e(e, "Error SpeakerEmbeddingExtractor")
        }

        return@withContext processEnrollmentStep(zipformerText, extractedEmbedding)
    }

    suspend fun recordAndVerifyVoiceUnlock(
        context: Context,
        langCode: String,
        targetPassphrase: String,
        referenceEmbedding: FloatArray?,
        onStatusUpdate: (String) -> Unit
    ): VoiceUnlockResult = withContext(Dispatchers.IO) {
        onStatusUpdate("Đang lắng nghe giọng nói (4s)...")

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val recorder = VoiceUtils.createAudioRecord(
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        ) ?: return@withContext VoiceUnlockResult.Failure(
            "",
            1.0f,
            0.0,
            "❌ Micro bận hoặc không thể khởi tạo!"
        )

        val audioData = mutableListOf<Float>()
        val buffer = ByteArray(512 * 2)

        val timerJob = launch {
            var leftSec = 4
            while (leftSec > 0) {
                withContext(Dispatchers.Main) {
                    onStatusUpdate("Đang lắng nghe giọng nói (${leftSec}s)...")
                }
                delay(1000L.milliseconds)
                leftSec--
            }
        }

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
            timerJob.cancel()
            try {
                recorder.stop()
            } catch (_: Exception) {
            }
            try {
                recorder.release()
            } catch (_: Exception) {
            }
        }

        val rawAudioArray = audioData.toFloatArray()
        val rms = VoiceUtils.calculateRms(rawAudioArray)
        if (rms < 0.008f) {
            return@withContext VoiceUnlockResult.Failure(
                "",
                1.0f,
                0.0,
                "❌ Im lặng! Vui lòng nói khẩu lệnh mở khóa."
            )
        }

        onStatusUpdate("Đang nhận diện giọng nói...")

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

        if (!hasHumanSpeech) {
            return@withContext VoiceUnlockResult.Failure(
                "",
                1.0f,
                0.0,
                "❌ Không nghe thấy giọng nói! Thử lại."
            )
        }

        // 2. Zipformer ASR Text Match
        if (!ModelManager.isModelDownloaded(context, langCode)) {
            return@withContext VoiceUnlockResult.Failure(
                "",
                1.0f,
                0.0,
                "⚠️ Vui lòng mở App tải mô hình cho $langCode!"
            )
        }

        var rawZipformerText = ""
        try {
            val recognizer = ModelManager.getOrNewRecognizer(context, langCode)
            val offlineStream = recognizer.createStream()
            offlineStream.acceptWaveform(rawAudioArray, 16000)
            recognizer.decode(offlineStream)
            val result = recognizer.getResult(offlineStream)
            rawZipformerText = result.text.trim()
            offlineStream.release()
        } catch (e: Throwable) {
            Timber.e(e, "Error Zipformer ASR on Overlay")
        }

        val zipformerText = VoiceUtils.cleanPassphraseText(rawZipformerText)
        if (zipformerText.length < 2) {
            return@withContext VoiceUnlockResult.Failure(
                "",
                1.0f,
                0.0,
                "❌ Không nghe thấy từ khóa khẩu lệnh rõ ràng!"
            )
        }

        // 3. Speaker Embedding Verification
        var isSpeakerMatched = false
        var calculatedDist = 1.0f
        if (referenceEmbedding != null) {
            try {
                val extractor = ModelManager.getOrNewExtractor(context)
                val stream = extractor.createStream()
                stream.acceptWaveform(rawAudioArray, 16000)
                stream.inputFinished()
                val testEmbedding = extractor.compute(stream)
                stream.release()

                calculatedDist = cosineDistance(testEmbedding, referenceEmbedding)
                if (calculatedDist < 0.45f) {
                    isSpeakerMatched = true
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error SpeakerEmbeddingExtractor on Overlay")
            }
        } else {
            isSpeakerMatched = true
        }

        val similarity = if (targetPassphrase.isNotBlank()) {
            stringSimilarity(zipformerText.lowercase(), targetPassphrase.lowercase())
        } else 0.0

        val isTextMatched = targetPassphrase.isBlank() || similarity >= 0.3 ||
                zipformerText.contains(targetPassphrase, ignoreCase = true) ||
                targetPassphrase.contains(zipformerText, ignoreCase = true)

        val isUnlockSuccessful = if (referenceEmbedding != null) {
            isSpeakerMatched && isTextMatched
        } else {
            isTextMatched
        }

        return@withContext if (isUnlockSuccessful) {
            VoiceUnlockResult.Success(zipformerText, calculatedDist, similarity)
        } else {
            val errorMsg = when {
                !isTextMatched && !isSpeakerMatched -> "❌ Khai báo sai khẩu lệnh và không đúng giọng nói!"
                !isTextMatched -> "❌ Sai khẩu lệnh từ khóa!"
                else -> "❌ Khẩu lệnh đúng nhưng KHÁC giọng người đăng ký!"
            }
            VoiceUnlockResult.Failure(zipformerText, calculatedDist, similarity, errorMsg)
        }
    }

    suspend fun processEnrollmentStep(
        recognizedText: String,
        embedding: FloatArray?
    ): EnrollmentResult = withContext(Dispatchers.Default) {
        when (currentEnrollmentStep) {
            1 -> {
                recordedEmbeddings.clear()
                if (embedding != null) {
                    recordedEmbeddings.add(embedding)
                }
                firstPassphrase = recognizedText
                currentEnrollmentStep = 2
                EnrollmentResult.StepSuccess(
                    step = 1,
                    passphrase = recognizedText,
                    message = "✅ Lần 1/3 OK: \"$recognizedText\". Nhấn Record nói Lần 2..."
                )
            }

            2 -> {
                val isMatched = VoiceUtils.isStrictTextMatch(recognizedText, firstPassphrase)

                if (isMatched) {
                    if (embedding != null) {
                        recordedEmbeddings.add(embedding)
                    }
                    currentEnrollmentStep = 3
                    EnrollmentResult.StepSuccess(
                        step = 2,
                        passphrase = firstPassphrase,
                        message = "✅ Lần 2/3 OK! Nhấn Record nói tiếp Lần 3"
                    )
                } else {
                    EnrollmentResult.MismatchError(
                        currentStep = 2,
                        expectedPassphrase = firstPassphrase,
                        message = "❌ Lần 2 (\"$recognizedText\") không khớp khẩu lệnh Lần 1 (\"$firstPassphrase\")!"
                    )
                }
            }

            else -> {
                val isMatched = VoiceUtils.isStrictTextMatch(recognizedText, firstPassphrase)

                if (isMatched) {
                    if (embedding != null) {
                        recordedEmbeddings.add(embedding)
                    }

                    val finalEmbedding = VoiceUtils.averageEmbeddings(recordedEmbeddings)
                    savePassphraseAndEmbedding(firstPassphrase, finalEmbedding)

                    EnrollmentResult.CompleteSuccess(
                        passphrase = firstPassphrase,
                        message = "🎉 Hoàn tất 3/3! Đã thiết lập thành công khẩu lệnh \"$firstPassphrase\""
                    )
                } else {
                    EnrollmentResult.MismatchError(
                        currentStep = 3,
                        expectedPassphrase = firstPassphrase,
                        message = "❌ Lần 3 (\"$recognizedText\") không khớp khẩu lệnh Lần 1 (\"$firstPassphrase\")!"
                    )
                }
            }
        }
    }

    suspend fun getSavedSpeechLanguage(): String = withContext(Dispatchers.IO) {
        preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
    }

    suspend fun saveSpeechLanguage(languageCode: String) = withContext(Dispatchers.IO) {
        LockScreenState.speechLanguageCode = languageCode
        preferencesDataStoreHelper.putString(PrefKeys.SELECTED_SPEECH_LANGUAGE, languageCode)
    }

    suspend fun savePassphraseAndEmbedding(
        passphrase: String,
        embedding: FloatArray,
        languageCode: String = LockScreenState.speechLanguageCode
    ) = withContext(Dispatchers.IO) {
        LockScreenState.voicePassphrase = passphrase
        LockScreenState.referenceEmbedding = embedding
        LockScreenState.speechLanguageCode = languageCode
        LockScreenState.isVoiceLockEnabled = true

        ServicePreferences.saveVoiceLock(App.instance, true, passphrase, languageCode)
        preferencesDataStoreHelper.putString(PrefKeys.VOICE_PASSPHRASE, passphrase)
        preferencesDataStoreHelper.putString(PrefKeys.SELECTED_SPEECH_LANGUAGE, languageCode)
        preferencesDataStoreHelper.putBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, true)
    }

    suspend fun deleteVoicePassphrase() = withContext(Dispatchers.IO) {
        LockScreenState.voicePassphrase = ""
        LockScreenState.referenceEmbedding = null
        LockScreenState.isVoiceLockEnabled = false

        ServicePreferences.saveVoiceLock(App.instance, false, "", "")
        preferencesDataStoreHelper.removeKey(PrefKeys.VOICE_PASSPHRASE)
        preferencesDataStoreHelper.putBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, false)
    }

    fun isModelDownloaded(context: Context, languageCode: String): Boolean {
        return ModelManager.isModelDownloaded(context, languageCode)
    }

    suspend fun downloadModel(
        context: Context,
        languageCode: String,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        ModelManager.downloadAndExtractModel(context, languageCode, onProgress)
    }
}
