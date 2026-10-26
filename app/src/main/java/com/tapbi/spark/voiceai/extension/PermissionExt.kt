package com.tapbi.spark.voiceai.extension

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasCameraPermission(): Boolean {
    return hasPermission(Manifest.permission.CAMERA)
}

fun Context.hasRecordAudioPermission(): Boolean {
    return hasPermission(Manifest.permission.RECORD_AUDIO)
}

fun Context.hasOverlayPermission(): Boolean {
    return !isAtLeastApi23 || Settings.canDrawOverlays(this)
}

fun Context.openOverlayPermissionSettings() {
    if (isAtLeastApi23) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}

fun Context.openAppPermissionSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
