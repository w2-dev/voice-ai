package com.tapbi.spark.voiceai.ui.main.voice

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.common.models.EnrollmentResult
import com.tapbi.spark.voiceai.data.repository.VoiceRepository
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

val SUPPORTED_LANGUAGES_MAP get() = Constant.SUPPORTED_SPEECH_LANGUAGES_MAP

data class VoiceSetupUiState(
    val enrollmentStep: Int = 1,
    val firstPassphrase: String = "",
    val isAutoListenEnabled: Boolean = false,
    val recordedPassphrase: String = LockScreenState.voicePassphrase,
    val recordedVectorInfo: String = if (LockScreenState.referenceEmbedding != null) "Vector [96d]: Đã lưu mẫu giọng nói chuẩn" else "",
    val isRecording: Boolean = false,
    val statusMessage: String = "Nhấn nút Record để thu âm Lần 1 (1/3)",
    val selectedLanguage: String = SUPPORTED_LANGUAGES_MAP[LockScreenState.speechLanguageCode]
        ?: "🇻🇳 Tiếng Việt (vi - int8)",
    val languageCode: String = LockScreenState.speechLanguageCode,
    val showDownloadDialog: Boolean = false,
    val downloadProgress: Int = 0,
    val isDownloadingModel: Boolean = false
)

@HiltViewModel
class VoiceSetupViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val navigation: MainNavigator
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(VoiceSetupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLang = voiceRepository.getSavedSpeechLanguage()
            LockScreenState.speechLanguageCode = savedLang
            refreshState()
        }
    }

    fun refreshState() {
        val currentCode = LockScreenState.speechLanguageCode
        val currentName = SUPPORTED_LANGUAGES_MAP[currentCode] ?: "🇻🇳 Tiếng Việt (vi - int8)"

        _uiState.update {
            it.copy(
                recordedPassphrase = LockScreenState.voicePassphrase,
                recordedVectorInfo = if (LockScreenState.referenceEmbedding != null) "Vector [96d]: Đã lưu mẫu giọng nói chuẩn" else "",
                languageCode = currentCode,
                selectedLanguage = currentName
            )
        }
    }

    fun selectLanguage(context: Context, languageCode: String, languageName: String) {
        _uiState.update {
            it.copy(
                selectedLanguage = languageName,
                languageCode = languageCode
            )
        }

        viewModelScope.launch {
            voiceRepository.saveSpeechLanguage(languageCode)
        }

        if (voiceRepository.isModelDownloaded(context, languageCode)) {
            _uiState.update {
                it.copy(
                    showDownloadDialog = false,
                    isDownloadingModel = false,
                    statusMessage = ""
                )
            }
        }
    }

    fun checkModelAndRecord(context: Context): Boolean {
        val currentLangCode = _uiState.value.languageCode
        val downloaded = voiceRepository.isModelDownloaded(context, currentLangCode)
        Timber.d("checkModelAndRecord: languageCode = $currentLangCode, isDownloaded = $downloaded")

        if (downloaded) {
            _uiState.update {
                it.copy(
                    showDownloadDialog = false,
                    isDownloadingModel = false,
                    statusMessage = ""
                )
            }
            return true
        } else {
            _uiState.update { it.copy(showDownloadDialog = true) }
            return false
        }
    }

    fun confirmDownloadModel(context: Context, onDownloadSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                showDownloadDialog = false,
                isDownloadingModel = true,
                downloadProgress = 0,
                statusMessage = "Đang tải mô hình Zipformer cho ${_uiState.value.selectedLanguage}..."
            )
        }

        viewModelScope.launch {
            val langCode = _uiState.value.languageCode
            val langName = _uiState.value.selectedLanguage
            val success = voiceRepository.downloadModel(context, langCode) { progress ->
                _uiState.update {
                    it.copy(
                        downloadProgress = progress,
                        statusMessage = "Đang tải $langName: $progress%"
                    )
                }
            }

            _uiState.update { it.copy(isDownloadingModel = false) }

            if (success) {
                _uiState.update { it.copy(statusMessage = "Đã tải xong mô hình $langName!") }
                onDownloadSuccess()
            } else {
                _uiState.update { it.copy(statusMessage = "Lỗi tải mô hình $langName. Vui lòng kiểm tra mạng.") }
            }
        }
    }

    fun dismissDownloadDialog() {
        _uiState.update { it.copy(showDownloadDialog = false) }
    }

    fun resetEnrollment() {
        voiceRepository.resetEnrollment()
        _uiState.update {
            it.copy(
                enrollmentStep = 1,
                firstPassphrase = "",
                statusMessage = "Nhấn nút Record để thu âm Lần 1 (1/3)"
            )
        }
    }

    fun processEnrollmentStep(
        recognizedText: String,
        embedding: FloatArray?,
        vectorPreview: String,
        onStepResult: (isSuccess: Boolean, message: String, isFinished: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = voiceRepository.processEnrollmentStep(recognizedText, embedding)) {
                is EnrollmentResult.StepSuccess -> {
                    _uiState.update {
                        it.copy(
                            enrollmentStep = voiceRepository.currentEnrollmentStep,
                            firstPassphrase = voiceRepository.firstPassphrase,
                            recordedPassphrase = recognizedText,
                            recordedVectorInfo = vectorPreview,
                            isRecording = false,
                            statusMessage = result.message
                        )
                    }
                    onStepResult(true, result.message, false)
                }
                is EnrollmentResult.MismatchError -> {
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            statusMessage = result.message
                        )
                    }
                    onStepResult(false, result.message, false)
                }
                is EnrollmentResult.CompleteSuccess -> {
                    val finalVectorInfo = "Vector [96d]: [Trung bình 3 lần ghi âm chuẩn]"
                    _uiState.update {
                        it.copy(
                            recordedPassphrase = result.passphrase,
                            recordedVectorInfo = finalVectorInfo,
                            isRecording = false,
                            statusMessage = result.message
                        )
                    }
                    onStepResult(true, result.message, true)
                }
            }
        }
    }

    fun openPassphraseSetup() {
        navigation.openPassphraseSetup()
    }

    fun openPremium() {
        navigation.navigateTo(NavigationScreen.PremiumScreen.route)
    }

    fun popBackStack() {
        navigation.popBackStack()
    }

    fun toggleAutoListen(enabled: Boolean) {
        _uiState.update { it.copy(isAutoListenEnabled = enabled) }
    }

    fun setRecording(isRecording: Boolean) {
        _uiState.update { it.copy(isRecording = isRecording) }
    }

    fun updateStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }
}
