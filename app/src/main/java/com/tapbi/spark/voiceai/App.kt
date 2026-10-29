package com.tapbi.spark.voiceai

import android.content.Context
import android.os.Build
import android.os.UserManager
import androidx.multidex.MultiDexApplication
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tapbi.spark.voiceai.ads.feature.BillingAppManager
import com.tapbi.spark.voiceai.utils.MyDebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initLog()

        if (isUserUnlocked()) {
            initUnlockedComponents()
        }
    }

    fun initUnlockedComponents() {
        try {
            false.enableFirebaseCrashlytics()
        } catch (e: Exception) {
            Timber.e(e, "Error initializing Crashlytics in App")
        }

        try {
            BillingAppManager.initResource(this)
        } catch (e: Exception) {
            Timber.e(e, "Error initializing BillingAppManager in App")
        }
    }

    private fun Boolean.enableFirebaseCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = this
        } catch (e: Exception) {
            Timber.e(e, "Crashlytics unavailable in Direct Boot")
        }
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(MyDebugTree())
        }
    }

    fun isUserUnlocked(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val userManager = getSystemService(Context.USER_SERVICE) as? UserManager
            return userManager?.isUserUnlocked ?: true
        }
        return true
    }

    companion object {
        lateinit var instance: App
    }
}
