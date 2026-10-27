package com.tapbi.spark.voiceai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.service.LockScreenService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesDataStoreHelper: PreferencesDataStoreHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Timber.d("BootReceiver: ACTION_BOOT_COMPLETED received")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
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

                    if (isActive && context.hasOverlayPermission()) {
                        val serviceIntent = Intent(context, LockScreenService::class.java)
                        if (isAtLeastApi26) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error starting LockScreenService on Boot")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
