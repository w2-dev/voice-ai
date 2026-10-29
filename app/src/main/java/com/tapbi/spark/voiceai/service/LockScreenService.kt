package com.tapbi.spark.voiceai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.data.preferences.ServicePreferences
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.isAtLeastApi29
import com.tapbi.spark.voiceai.extension.isAtLeastApi33
import com.tapbi.spark.voiceai.extension.isAtLeastApi34
import com.tapbi.spark.voiceai.ui.main.lockoverlay.LockScreenViewBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenService : Service() {

    @Inject
    lateinit var preferencesDataStoreHelper: PreferencesDataStoreHelper

    private var viewBinder: LockScreenViewBinder? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var internalReceiver: BroadcastReceiver? = null
    private var isScreenOff = false

    companion object {
        private const val TAG = "LockScreenService"
        const val CHANNEL_ID = "LockScreenServiceChannel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("onCreate() - Starting Foreground LockScreenService")
        createNotificationChannel()
        val notification = createNotification()

        if (isAtLeastApi34) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error startForeground API 34 specialUse")
                startForeground(NOTIFICATION_ID, notification)
            }
        } else if (isAtLeastApi29) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                )
            } catch (_: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        initViewBinder()
        registerScreenReceiver()
        loadLockScreenState()
    }

    private fun loadLockScreenState(onLoaded: (() -> Unit)? = null) {
        // Load synchronously from ServicePreferences first (instantaneous < 1ms)
        ServicePreferences.loadLockScreenStateSync(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isActive = ServicePreferences.isServiceEnabled(this@LockScreenService) ||
                        preferencesDataStoreHelper.getBoolean(PrefKeys.IS_LOCK_SCREEN_ACTIVE, false)
                val isVoice =
                    preferencesDataStoreHelper.getBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, false)
                val voicePass = preferencesDataStoreHelper.getString(PrefKeys.VOICE_PASSPHRASE, "")
                val speechLang =
                    preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
                val isPin =
                    preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, false)
                val pinPass = preferencesDataStoreHelper.getString(PrefKeys.PIN_PASSWORD, "")
                val isPattern =
                    preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, false)
                val patternStr =
                    preferencesDataStoreHelper.getString(PrefKeys.PATTERN_LOCK_DATA, "")
                val isFingerprint =
                    preferencesDataStoreHelper.getBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
                val qIndex = preferencesDataStoreHelper.getInt(PrefKeys.SECURITY_QUESTION_INDEX, 0)
                val qAnswer = preferencesDataStoreHelper.getString(PrefKeys.SECURITY_ANSWER, "")

                val pRows = preferencesDataStoreHelper.getInt(PrefKeys.PATTERN_GRID_ROWS, 3)
                val pCols = preferencesDataStoreHelper.getInt(PrefKeys.PATTERN_GRID_COLS, 3)

                ServicePreferences.syncFromDataStoreOrPrefs(
                    context = this@LockScreenService,
                    isActive = isActive,
                    isVoice = isVoice,
                    voicePass = voicePass,
                    speechLang = speechLang,
                    isPin = isPin,
                    pinPass = pinPass,
                    isPattern = isPattern,
                    patternRows = pRows,
                    patternCols = pCols,
                    patternStr = patternStr,
                    isFingerprint = isFingerprint,
                    qIndex = qIndex, qAnswer = qAnswer
                )

                Timber.tag(TAG).d("LockScreenState loaded asynchronously from DataStore")
                withContext(Dispatchers.Main) {
                    onLoaded?.invoke()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error loading LockScreenState from DataStore")
            }
        }
    }

    private fun initViewBinder() {
        if (viewBinder == null) {
            viewBinder = LockScreenViewBinder(this) {
                Timber.tag(TAG).d("Unlock requested from ViewBinder")
                hideLockScreen()
            }
        }
    }

    private fun createNotificationChannel() {
        if (isAtLeastApi26) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Lock Screen Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice AI Lock Screen Active")
            .setContentText("Dịch vụ khóa màn hình đang chạy ngầm")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            screenReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Timber.tag(TAG).d("screenReceiver onReceive: action = ${intent?.action}")
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_OFF -> {
                            Timber.tag(TAG).d("ACTION_SCREEN_OFF received. Showing lock overlay.")
                            isScreenOff = true
                            showLockScreen()
                        }

                        Intent.ACTION_SCREEN_ON -> {
                            Timber.tag(TAG).d("ACTION_SCREEN_ON received. Showing lock overlay.")
                            showLockScreen()
                        }

                        Intent.ACTION_USER_PRESENT -> {
                            Timber.tag(TAG).d("ACTION_USER_PRESENT received.")
                        }
                    }
                }
            }

            val screenFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }

            try {
                if (isAtLeastApi33) {
                    ContextCompat.registerReceiver(
                        this,
                        screenReceiver,
                        screenFilter,
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    registerReceiver(screenReceiver, screenFilter)
                }
                Timber.tag(TAG).d("ScreenReceiver registered successfully.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error registering ScreenReceiver")
            }
        }

        if (internalReceiver == null) {
            internalReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Timber.tag(TAG).d("internalReceiver onReceive: action = ${intent?.action}")
                    when (intent?.action) {
                        Constant.ACTION_BIOMETRIC_STARTED -> {
                            Timber.tag(TAG).d("ACTION_BIOMETRIC_STARTED received.")
                        }

                        Constant.ACTION_BIOMETRIC_CANCELLED -> {
                            Timber.tag(TAG).d("ACTION_BIOMETRIC_CANCELLED received.")
                        }

                        Constant.ACTION_BIOMETRIC_UNLOCK,
                        Constant.ACTION_SECURITY_QUESTION_UNLOCK -> {
                            Timber.tag(TAG).d("Unlock action received via BroadcastReceiver")
                            hideLockScreen()
                        }
                    }
                }
            }

            val internalFilter = IntentFilter().apply {
                addAction(Constant.ACTION_BIOMETRIC_STARTED)
                addAction(Constant.ACTION_BIOMETRIC_CANCELLED)
                addAction(Constant.ACTION_BIOMETRIC_UNLOCK)
                addAction(Constant.ACTION_SECURITY_QUESTION_UNLOCK)
            }

            try {
                ContextCompat.registerReceiver(
                    this,
                    internalReceiver,
                    internalFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                Timber.tag(TAG).d("InternalReceiver registered successfully.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error registering InternalReceiver")
            }
        }
    }

    private fun showLockScreen() {
        ServicePreferences.loadLockScreenStateSync(this)
        val isActive =
            ServicePreferences.isServiceEnabled(this) || LockScreenState.isLockScreenActive
        Timber.tag(TAG)
            .d("showLockScreen() called | isLockScreenActive = $isActive")
        if (!isActive) {
            Timber.tag(TAG).d("Lock screen active switch is OFF, skipping overlay show")
            return
        }
        initViewBinder()
        try {
            viewBinder?.show()
        } catch (t: Throwable) {
            Timber.tag(TAG).e(t, "Error showing lock screen overlay")
        }
    }

    private fun hideLockScreen() {
        Timber.tag(TAG).d("hideLockScreen() called")
        viewBinder?.hide()
        isScreenOff = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).d("onStartCommand() action = ${intent?.action}")
        initViewBinder()
        registerScreenReceiver()
        loadLockScreenState()

        if (intent?.getBooleanExtra("SHOW_IMMEDIATELY", false) == true) {
            showLockScreen()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).d("onDestroy()")
        hideLockScreen()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
                Timber.tag(TAG).d("ScreenReceiver unregistered.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error unregistering ScreenReceiver")
            }
            screenReceiver = null
        }
        internalReceiver?.let {
            try {
                unregisterReceiver(it)
                Timber.tag(TAG).d("InternalReceiver unregistered.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error unregistering InternalReceiver")
            }
            internalReceiver = null
        }
    }
}
