package com.tapbi.spark.voiceai.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.tapbi.spark.voiceai.common.models.LockScreenState

object ServicePreferences {
    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val directBootContext = context.createDeviceProtectedStorageContext() ?: context
            directBootContext.getSharedPreferences(
                PrefKeys.PREF_LOCK_SERVICE_NAME,
                Context.MODE_PRIVATE
            )
        } catch (_: Exception) {
            context.getSharedPreferences(PrefKeys.PREF_LOCK_SERVICE_NAME, Context.MODE_PRIVATE)
        }
    }

    fun setServiceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_LOCK_SCREEN_ACTIVE, enabled)
        }
    }

    fun isServiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(PrefKeys.KEY_IS_LOCK_SCREEN_ACTIVE, false)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(PrefKeys.KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_FIRST_LAUNCH, isFirst)
        }
    }

    fun isSplashCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(PrefKeys.KEY_IS_SPLASH_COMPLETED, false)
    }

    fun setSplashCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_SPLASH_COMPLETED, completed)
        }
    }

    fun saveVoiceLock(
        context: Context,
        enabled: Boolean,
        passphrase: String? = null,
        languageCode: String? = null
    ) {
        val finalPass = passphrase ?: LockScreenState.voicePassphrase
        val finalLang = languageCode ?: LockScreenState.speechLanguageCode
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_VOICE_LOCK_ENABLED, enabled)
            if (finalPass.isNotBlank()) putString(PrefKeys.KEY_VOICE_PASSPHRASE, finalPass)
            if (finalLang.isNotBlank()) putString(PrefKeys.KEY_SELECTED_SPEECH_LANGUAGE, finalLang)
        }
    }

    fun savePinLock(context: Context, enabled: Boolean, pinPassword: String? = null) {
        val finalPin = pinPassword ?: LockScreenState.pinPassword
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_PIN_LOCK_ENABLED, enabled)
            if (finalPin.isNotBlank()) putString(PrefKeys.KEY_PIN_PASSWORD, finalPin)
        }
    }

    fun savePatternLock(
        context: Context,
        enabled: Boolean,
        rows: Int? = null,
        cols: Int? = null,
        patternData: String? = null
    ) {
        val finalRows = rows ?: LockScreenState.patternRows
        val finalCols = cols ?: LockScreenState.patternCols
        val finalData = patternData ?: LockScreenState.targetPattern?.joinToString(",") ?: ""
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_PATTERN_LOCK_ENABLED, enabled)
            putInt(PrefKeys.KEY_PATTERN_GRID_ROWS, finalRows)
            putInt(PrefKeys.KEY_PATTERN_GRID_COLS, finalCols)
            if (finalData.isNotBlank()) putString(PrefKeys.KEY_PATTERN_LOCK_DATA, finalData)
        }
    }

    fun saveFingerprintLock(context: Context, enabled: Boolean) {
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_FINGERPRINT_ENABLED, enabled)
        }
    }

    fun saveSecurityQuestion(context: Context, index: Int, answer: String) {
        getPrefs(context).edit(commit = true) {
            putInt(PrefKeys.KEY_SECURITY_QUESTION_INDEX, index)
            if (answer.isNotBlank()) putString(PrefKeys.KEY_SECURITY_ANSWER, answer)
        }
    }

    fun syncFromDataStoreOrPrefs(
        context: Context,
        isActive: Boolean,
        isVoice: Boolean,
        voicePass: String,
        speechLang: String,
        isPin: Boolean,
        pinPass: String,
        isPattern: Boolean,
        patternRows: Int,
        patternCols: Int,
        patternStr: String,
        isFingerprint: Boolean,
        qIndex: Int,
        qAnswer: String
    ) {
        getPrefs(context).edit(commit = true) {
            putBoolean(PrefKeys.KEY_IS_LOCK_SCREEN_ACTIVE, isActive)
            putBoolean(PrefKeys.KEY_IS_VOICE_LOCK_ENABLED, isVoice)
            if (voicePass.isNotBlank()) putString(PrefKeys.KEY_VOICE_PASSPHRASE, voicePass)
            if (speechLang.isNotBlank()) putString(PrefKeys.KEY_SELECTED_SPEECH_LANGUAGE, speechLang)

            putBoolean(PrefKeys.KEY_IS_PIN_LOCK_ENABLED, isPin)
            if (pinPass.isNotBlank()) putString(PrefKeys.KEY_PIN_PASSWORD, pinPass)

            putBoolean(PrefKeys.KEY_IS_PATTERN_LOCK_ENABLED, isPattern)
            putInt(PrefKeys.KEY_PATTERN_GRID_ROWS, patternRows)
            putInt(PrefKeys.KEY_PATTERN_GRID_COLS, patternCols)
            if (patternStr.isNotBlank()) putString(PrefKeys.KEY_PATTERN_LOCK_DATA, patternStr)

            putBoolean(PrefKeys.KEY_IS_FINGERPRINT_ENABLED, isFingerprint)
            putInt(PrefKeys.KEY_SECURITY_QUESTION_INDEX, qIndex)
            if (qAnswer.isNotBlank()) putString(PrefKeys.KEY_SECURITY_ANSWER, qAnswer)
        }

        LockScreenState.isLockScreenActive = isActive
        LockScreenState.isVoiceLockEnabled = isVoice
        if (voicePass.isNotBlank()) LockScreenState.voicePassphrase = voicePass
        if (speechLang.isNotBlank()) LockScreenState.speechLanguageCode = speechLang
        LockScreenState.isPinLockEnabled = isPin
        if (pinPass.isNotBlank()) LockScreenState.pinPassword = pinPass
        LockScreenState.isPatternLockEnabled = isPattern
        LockScreenState.patternRows = patternRows
        LockScreenState.patternCols = patternCols
        if (patternStr.isNotBlank()) {
            LockScreenState.targetPattern =
                patternStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
        LockScreenState.isFingerprintEnabled = isFingerprint
        LockScreenState.securityQuestionIndex = qIndex
        if (qAnswer.isNotBlank()) LockScreenState.securityAnswer = qAnswer
    }

    fun loadLockScreenStateSync(context: Context) {
        val prefs = getPrefs(context)
        val isActive = prefs.getBoolean(PrefKeys.KEY_IS_LOCK_SCREEN_ACTIVE, false)
        val isVoice = prefs.getBoolean(PrefKeys.KEY_IS_VOICE_LOCK_ENABLED, false)
        val voicePass = prefs.getString(PrefKeys.KEY_VOICE_PASSPHRASE, "") ?: ""
        val speechLang = prefs.getString(PrefKeys.KEY_SELECTED_SPEECH_LANGUAGE, "vi-VN") ?: "vi-VN"
        val isPin = prefs.getBoolean(PrefKeys.KEY_IS_PIN_LOCK_ENABLED, false)
        val pinPass = prefs.getString(PrefKeys.KEY_PIN_PASSWORD, "") ?: ""
        val isPattern = prefs.getBoolean(PrefKeys.KEY_IS_PATTERN_LOCK_ENABLED, false)
        val patternRows = prefs.getInt(PrefKeys.KEY_PATTERN_GRID_ROWS, 3)
        val patternCols = prefs.getInt(PrefKeys.KEY_PATTERN_GRID_COLS, 3)
        val patternStr = prefs.getString(PrefKeys.KEY_PATTERN_LOCK_DATA, "") ?: ""
        val isFingerprint = prefs.getBoolean(PrefKeys.KEY_IS_FINGERPRINT_ENABLED, false)
        val qIndex = prefs.getInt(PrefKeys.KEY_SECURITY_QUESTION_INDEX, 0)
        val qAnswer = prefs.getString(PrefKeys.KEY_SECURITY_ANSWER, "") ?: ""

        LockScreenState.isLockScreenActive = isActive
        LockScreenState.isVoiceLockEnabled = isVoice
        LockScreenState.voicePassphrase = voicePass
        LockScreenState.speechLanguageCode = speechLang
        LockScreenState.isPinLockEnabled = isPin
        LockScreenState.pinPassword = pinPass
        LockScreenState.isPatternLockEnabled = isPattern
        LockScreenState.patternRows = patternRows
        LockScreenState.patternCols = patternCols
        if (patternStr.isNotBlank()) {
            LockScreenState.targetPattern =
                patternStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else {
            LockScreenState.targetPattern = null
        }
        LockScreenState.isFingerprintEnabled = isFingerprint
        LockScreenState.securityQuestionIndex = qIndex
        LockScreenState.securityAnswer = qAnswer
    }
}
