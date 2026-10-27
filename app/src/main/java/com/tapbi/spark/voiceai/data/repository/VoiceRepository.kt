package com.tapbi.spark.voiceai.data.repository

import android.content.Context
import com.tapbi.spark.voiceai.common.models.EnrollmentResult
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.utils.ModelManager
import com.tapbi.spark.voiceai.utils.VoiceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepository @Inject constructor(
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) {
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

        preferencesDataStoreHelper.putString(PrefKeys.VOICE_PASSPHRASE, passphrase)
        preferencesDataStoreHelper.putString(PrefKeys.SELECTED_SPEECH_LANGUAGE, languageCode)
        preferencesDataStoreHelper.putBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, true)
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
