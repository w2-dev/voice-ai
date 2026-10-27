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
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.isAtLeastApi29
import com.tapbi.spark.voiceai.extension.isAtLeastApi33
import com.tapbi.spark.voiceai.extension.isAtLeastApi34
import com.tapbi.spark.voiceai.ui.main.lockoverlay.LockScreenViewBinder
import timber.log.Timber

class LockScreenService : Service() {

    private var viewBinder: LockScreenViewBinder? = null
    private var screenReceiver: BroadcastReceiver? = null
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
        if (screenReceiver != null) return

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Timber.tag(TAG).d("BroadcastReceiver onReceive: action = ${intent?.action}")
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        Timber.tag(TAG).d("ACTION_SCREEN_OFF received.")
                        isScreenOff = true
                    }

                    Intent.ACTION_SCREEN_ON -> {
                        Timber.tag(TAG).d("ACTION_SCREEN_ON received. Showing overlay.")
                        showLockScreen()
                    }

                    Intent.ACTION_USER_PRESENT -> {
                        Timber.tag(TAG).d("ACTION_USER_PRESENT received.")
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        try {
            if (isAtLeastApi33) {
                ContextCompat.registerReceiver(
                    this,
                    screenReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(screenReceiver, filter)
            }
            Timber.tag(TAG).d("ScreenReceiver registered successfully.")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error registering ScreenReceiver")
        }
    }

    private fun showLockScreen() {
        Timber.tag(TAG).d("showLockScreen() called")
        initViewBinder()
        viewBinder?.show()
    }

    private fun hideLockScreen() {
        Timber.tag(TAG).d("hideLockScreen() called")
        viewBinder?.hide()
        isScreenOff = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).d("onStartCommand()")
        initViewBinder()
        registerScreenReceiver()
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
                Timber.tag(TAG).e(e, "Error unregistering receiver")
            }
            screenReceiver = null
        }
    }
}
