package com.tapbi.spark.voiceai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.data.preferences.ServicePreferences
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.service.LockScreenService
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.d("BootReceiver: received action = $action")

        try {
            if (App.instance.isUserUnlocked()) {
                App.instance.initUnlockedComponents()
            }
        } catch (_: Exception) {
        }

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_USER_UNLOCKED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val isServiceEnabled = ServicePreferences.isServiceEnabled(context)
            val hasOverlay = context.hasOverlayPermission()

            Timber.d("BootReceiver: isServiceEnabled = $isServiceEnabled, hasOverlay = $hasOverlay")

            if (isServiceEnabled && hasOverlay) {
                try {
                    val serviceIntent = Intent(context, LockScreenService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                    Timber.d("BootReceiver: LockScreenService started successfully on boot")
                } catch (e: Exception) {
                    Timber.e(e, "BootReceiver: Error starting LockScreenService on boot")
                }
            } else {
                Timber.d("BootReceiver: Skipped starting service (enabled=$isServiceEnabled, overlay=$hasOverlay)")
            }
        }
    }
}
