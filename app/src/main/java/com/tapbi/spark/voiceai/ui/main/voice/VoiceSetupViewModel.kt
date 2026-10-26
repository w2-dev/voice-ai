package com.tapbi.spark.voiceai.ui.main.voice

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import com.tapbi.spark.voiceai.utils.ModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

val SUPPORTED_LANGUAGES_MAP = mapOf(
    "vi-VN" to "🇻🇳 Tiếng Việt (vi - int8)",
    "ru-RU" to "🇷🇺 Tiếng Nga (ru - int8)",
    "en-US" to "🇺🇸 Tiếng Anh (en - int8)",
    "zh-CN" to "🇨🇳 Tiếng Trung (zh - int8)",
    "ja-JP" to "🇯🇵 Tiếng Nhật (ja - int8)",
    "ko-KR" to "🇰🇷 Tiếng Hàn (ko - int8)",
    "th-TH" to "🇹🇭 Tiếng Thái (th - int8)",
    "yue-HK" to "🇭🇰 Tiếng Quảng Đông (yue - int8)"
)

data class VoiceSetupUiState(
    val isAutoListenEnabled: Boolean = false,
    val recordedPassphrase: String = LockScreenState.voicePassphrase,
    val recordedVectorInfo: String = if (LockScreenState.referenceEmbedding != null) "Vector [96d]: Đã lưu mẫu giọng nói chuẩn" else "",
    val isRecording: Boolean = false,
    val statusMessage: String = "",
    val selectedLanguage: String = SUPPORTED_LANGUAGES_MAP[LockScreenState.speechLanguageCode] ?: "🇻🇳 Tiếng Việt (vi - int8)",
    val languageCode: String = LockScreenState.speechLanguageCode,
    val showDownloadDialog: Boolean = false,
    val downloadProgress: Int = 0,
    val isDownloadingModel: Boolean = false
)

@HiltViewModel
class VoiceSetupViewModel @Inject constructor(
    private val navigation: MainNavigator,
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(VoiceSetupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLang = preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
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
        LockScreenState.speechLanguageCode = languageCode

        viewModelScope.launch {
            preferencesDataStoreHelper.putString(PrefKeys.SELECTED_SPEECH_LANGUAGE, languageCode)
        }

        if (ModelManager.isModelDownloaded(context, languageCode)) {
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
        val downloaded = ModelManager.isModelDownloaded(context, currentLangCode)
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
            val success = ModelManager.downloadAndExtractModel(context, langCode) { progress ->
                _uiState.update { it.copy(downloadProgress = progress, statusMessage = "Đang tải $langName: $progress%") }
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

    fun updateRecordedVectorInfo(info: String) {
        _uiState.update { it.copy(recordedVectorInfo = info) }
    }

    fun updateRecordedPassphrase(passphrase: String) {
        if (passphrase.isNotBlank()) {
            LockScreenState.voicePassphrase = passphrase
            LockScreenState.isVoiceLockEnabled = true
            viewModelScope.launch {
                preferencesDataStoreHelper.putString(PrefKeys.VOICE_PASSPHRASE, passphrase)
                preferencesDataStoreHelper.putBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, true)
            }
        }
        _uiState.update {
            it.copy(
                recordedPassphrase = passphrase,
                isRecording = false,
                statusMessage = if (passphrase.isNotBlank()) "Recorded successfully!" else ""
            )
        }
    }
}
