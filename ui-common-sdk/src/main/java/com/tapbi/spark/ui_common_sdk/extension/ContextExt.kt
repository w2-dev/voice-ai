package com.tapbi.spark.ui_common_sdk.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.Settings
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Toast

val Context.hasFlash
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

val Context.hasNavigationBar: Boolean
    get() {
        val hasNavBarId = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (hasNavBarId > 0) {
            resources.getBoolean(hasNavBarId)
        } else {
            val hasPermanentMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
            val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val realSize = Point()
            val screenSize = Point()

            display.getRealSize(realSize)
            display.getSize(screenSize)

            realSize.y != screenSize.y || realSize.x != screenSize.x || !hasPermanentMenuKey
        }
    }

fun Context.copyToClipboard(text: String) {
    val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    val clip = ClipData.newPlainText("copied_text", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.shareText(text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
//    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "test")
    startActivity(Intent.createChooser(shareIntent, "Share..."))
}

fun Context.showToast(mess: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, mess, duration).show()
}

fun Context.openSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.setData(uri)
    startActivity(intent)
}

fun Context.openNetworkSetting() {
    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
    startActivity(intent)
}

fun Context.turnOnFlash() {
    toggleFlash(true)
}

fun Context.turnOffFlash() {
    toggleFlash(false)
}

private fun Context.toggleFlash(enabled: Boolean) {
    if (!hasFlash) return
    try {
        val cameraManager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val isBackCamera =
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            hasFlash && isBackCamera
        } ?: return

        cameraManager.setTorchMode(cameraId, enabled)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}