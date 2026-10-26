package com.tapbi.spark.voiceai.extension

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.request.ImageRequest
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.utils.DeviceUtils

val Context.prefsDatastore by preferencesDataStore(name = "prefs_data_store")

fun Context.isPinShortcutSupported(): Boolean {
    return if (isAtLeastApi26) {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager?.isRequestPinShortcutSupported == true
    } else {
        true
    }
}

fun Context.showToast(mess: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, mess, duration).show()
}

fun Context.shareText(text: String?, subject: String?) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.setType("text/plain")
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    startActivity(
        Intent.createChooser(shareIntent, "Share...")
    )
}

fun Context.shareApp() {
    val text =
        (getString(R.string.app_name) + " " + getString(R.string.link_share_app) + packageName)
    shareText(text, getString(R.string.app_name))
}

fun Context.feedBack() {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.type = "text/email"
    emailIntent.setPackage("com.google.android.gm")
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
    var versionCode: String? = "1.0"
    try {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        versionCode = pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    val androidVersion = Build.VERSION.SDK_INT
    emailIntent.putExtra(
        Intent.EXTRA_SUBJECT,
        "App Report (" + packageName + ") - version [" + versionCode + "] - [" + DeviceUtils.getDeviceName() + "] - Android[" + androidVersion + "]"
    )
    emailIntent.putExtra(Intent.EXTRA_TEXT, "")
    startActivity(Intent.createChooser(emailIntent, "Send mail Report App !"))
}

fun Context.openCHPlay() {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$packageName".toUri()
        )
    )
}

suspend fun Context.urlToBitmap(url: String): Bitmap? {
    val loader = ImageLoader(this)

    val request = ImageRequest.Builder(this)
        .data(url)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)

    return (result.drawable as? BitmapDrawable)?.bitmap
}

fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}