package com.tapbi.spark.voiceai.ui.main.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.isAtLeastApi30
import com.tapbi.spark.voiceai.extension.openOverlayPermissionSettings
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.service.LockScreenService
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val navigation: MainNavigator,
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val isActive =
                preferencesDataStoreHelper.getBoolean(PrefKeys.IS_LOCK_SCREEN_ACTIVE, false)
            val isVoice =
                preferencesDataStoreHelper.getBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, false)
            val voicePass = preferencesDataStoreHelper.getString(PrefKeys.VOICE_PASSPHRASE, "")
            val speechLang =
                preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
            val isPin = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, false)
            val pinPass = preferencesDataStoreHelper.getString(PrefKeys.PIN_PASSWORD, "")
            val isPattern =
                preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, false)
            val patternStr = preferencesDataStoreHelper.getString(PrefKeys.PATTERN_LOCK_DATA, "")
            val pRows = preferencesDataStoreHelper.getInt(PrefKeys.PATTERN_GRID_ROWS, 3)
            val pCols = preferencesDataStoreHelper.getInt(PrefKeys.PATTERN_GRID_COLS, 3)
            val isFingerprintSaved =
                preferencesDataStoreHelper.getBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
            val biometricManager = BiometricManager.from(context)
            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            val isFingerprintAvailable =
                biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
            val isFingerprint = isFingerprintSaved && isFingerprintAvailable

            val qIndex = preferencesDataStoreHelper.getInt(PrefKeys.SECURITY_QUESTION_INDEX, 0)
            val qAnswer = preferencesDataStoreHelper.getString(PrefKeys.SECURITY_ANSWER, "")

            LockScreenState.isLockScreenActive = isActive
            LockScreenState.isVoiceLockEnabled = isVoice
            LockScreenState.voicePassphrase = voicePass
            LockScreenState.speechLanguageCode = speechLang
            LockScreenState.isPinLockEnabled = isPin
            LockScreenState.pinPassword = pinPass
            LockScreenState.isPatternLockEnabled = isPattern
            LockScreenState.patternRows = pRows
            LockScreenState.patternCols = pCols
            if (patternStr.isNotBlank()) {
                LockScreenState.targetPattern =
                    patternStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            }
            LockScreenState.isFingerprintEnabled = isFingerprint
            LockScreenState.securityQuestionIndex = qIndex
            LockScreenState.securityAnswer = qAnswer

            ensureLockServiceStarted()

            _uiState.update {
                it.copy(
                    isLockScreenActive = isActive,
                    isVoiceLockEnabled = isVoice,
                    isPinLockEnabled = isPin,
                    isPatternLockEnabled = isPattern,
                    isFingerprintEnabled = isFingerprint
                )
            }
        }
    }

    fun ensureLockServiceStarted() {
        if (LockScreenState.isLockScreenActive && context.hasOverlayPermission()) {
            val intent = Intent(context, LockScreenService::class.java)
            if (isAtLeastApi26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun toggleLockScreenActive(context: Context, enabled: Boolean) {
        if (enabled) {
            // Check Overlay Permission using extension
            if (!context.hasOverlayPermission()) {
                context.openOverlayPermissionSettings()
                return
            }

            // Start Foreground Service
            val intent = Intent(context, LockScreenService::class.java)
            if (isAtLeastApi26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            // Stop Foreground Service
            val intent = Intent(context, LockScreenService::class.java)
            context.stopService(intent)
        }

        LockScreenState.isLockScreenActive = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_LOCK_SCREEN_ACTIVE, enabled)
        }
        _uiState.update { it.copy(isLockScreenActive = enabled) }
    }

    fun toggleVoiceLock(enabled: Boolean) {
        if (enabled && !LockScreenState.hasVoiceSetup) {
            navigation.openVoiceSetup()
            return
        }
        LockScreenState.isVoiceLockEnabled = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, enabled)
        }
        _uiState.update { it.copy(isVoiceLockEnabled = enabled) }
    }

    fun togglePinLock(enabled: Boolean) {
        if (enabled && !LockScreenState.hasPinSetup) {
            navigation.openPinSetup()
            return
        }
        LockScreenState.isPinLockEnabled = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, enabled)
        }
        _uiState.update { it.copy(isPinLockEnabled = enabled) }
    }

    fun togglePatternLock(enabled: Boolean) {
        if (enabled && !LockScreenState.hasPatternSetup) {
            navigation.openPatternTypeSelection()
            return
        }
        LockScreenState.isPatternLockEnabled = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, enabled)
        }
        _uiState.update { it.copy(isPatternLockEnabled = enabled) }
    }

    fun toggleFingerprint(context: Context, enabled: Boolean) {
        if (enabled) {
            setupFingerprintAuth(context)
        } else {
            LockScreenState.isFingerprintEnabled = false
            viewModelScope.launch {
                preferencesDataStoreHelper.putBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
            }
            _uiState.update { it.copy(isFingerprintEnabled = false) }
        }
    }

    fun onVoiceSetupClick() {
        navigation.openVoiceSetup()
    }

    fun onPinSetupClick() {
        navigation.openPinSetup()
    }

    fun onPatternSetupClick() {
        navigation.openPatternTypeSelection()
    }

    fun onFingerprintClick(context: Context) {
        setupFingerprintAuth(context)
    }

    fun onSecurityQuestionSetupClick() {
        navigation.openSecurityQuestionSetup()
    }

    fun dismissFingerprintSetupDialog() {
        _uiState.update { it.copy(showFingerprintSetupDialog = false) }
    }

    fun openSystemBiometricSettings(context: Context) {
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        try {
            val enrollIntent = if (isAtLeastApi30) {
                Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        authenticators
                    )
                }
            } else {
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            }
            context.startActivity(enrollIntent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun setupFingerprintAuth(context: Context) {
        val biometricManager = BiometricManager.from(context)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                LockScreenState.isFingerprintEnabled = true
                viewModelScope.launch {
                    preferencesDataStoreHelper.putBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, true)
                }
                _uiState.update { it.copy(isFingerprintEnabled = true) }
                Toast.makeText(
                    context,
                    "✅ Đã bật mở khóa bằng Vân tay/Sinh trắc học!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                LockScreenState.isFingerprintEnabled = false
                viewModelScope.launch {
                    preferencesDataStoreHelper.putBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
                }
                _uiState.update {
                    it.copy(
                        isFingerprintEnabled = false,
                        showFingerprintSetupDialog = true
                    )
                }
            }

            else -> {
                LockScreenState.isFingerprintEnabled = false
                viewModelScope.launch {
                    preferencesDataStoreHelper.putBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
                }
                _uiState.update { it.copy(isFingerprintEnabled = false) }
                Toast.makeText(
                    context,
                    "Thiết bị không hỗ trợ cảm biến vân tay / sinh trắc học!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun onLockScreenThemesClick() {
    }

    fun onSetupAiProtectionClick() {
        showPremiumScreen()
    }

    fun showPremiumScreen() {
        navigation.navigateTo(NavigationScreen.PremiumScreen.route)
    }
}
