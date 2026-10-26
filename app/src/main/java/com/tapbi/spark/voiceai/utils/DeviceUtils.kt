package com.tapbi.spark.voiceai.utils

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.tapbi.spark.voiceai.extension.capitalizeWords


object DeviceUtils {
    fun isSystemDarkModeEnabled(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return model.capitalizeWords()
        }
        return manufacturer.capitalizeWords() + " " + model
    }
}