package com.tapbi.spark.voiceai.ui.main.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.openOverlayPermissionSettings
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.service.LockScreenService
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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
            val isActive = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_LOCK_SCREEN_ACTIVE, false)
            val isVoice = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, false)
            val voicePass = preferencesDataStoreHelper.getString(PrefKeys.VOICE_PASSPHRASE, "")
            val speechLang = preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
            val isPin = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, false)
            val pinPass = preferencesDataStoreHelper.getString(PrefKeys.PIN_PASSWORD, "")
            val isPattern = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, false)
            val patternStr = preferencesDataStoreHelper.getString(PrefKeys.PATTERN_LOCK_DATA, "")
            val isFingerprint = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)

            LockScreenState.isLockScreenActive = isActive
            LockScreenState.isVoiceLockEnabled = isVoice
            LockScreenState.voicePassphrase = voicePass
            LockScreenState.speechLanguageCode = speechLang
            LockScreenState.isPinLockEnabled = isPin
            LockScreenState.pinPassword = pinPass
            LockScreenState.isPatternLockEnabled = isPattern
            if (patternStr.isNotBlank()) {
                LockScreenState.targetPattern = patternStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            }
            LockScreenState.isFingerprintEnabled = isFingerprint

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
            // Require PIN setup
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
            navigation.openPatternSetup()
            return
        }
        LockScreenState.isPatternLockEnabled = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, enabled)
        }
        _uiState.update { it.copy(isPatternLockEnabled = enabled) }
    }

    fun toggleFingerprint(enabled: Boolean) {
        LockScreenState.isFingerprintEnabled = enabled
        viewModelScope.launch {
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, enabled)
        }
        _uiState.update { it.copy(isFingerprintEnabled = enabled) }
    }

    fun onVoiceSetupClick() {
        navigation.openVoiceSetup()
    }

    fun onPinSetupClick() {
    }

    fun onPatternSetupClick() {
        navigation.openPatternSetup()
    }

    fun onFingerprintClick() {
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
