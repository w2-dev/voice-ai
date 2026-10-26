package com.tapbi.spark.voiceai

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
        false.enableFirebaseCrashlytics()
        initLog()
        BillingAppManager.initResource(this)
    }

    private fun Boolean.enableFirebaseCrashlytics() {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = this
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(MyDebugTree())
        }
    }

    companion object {
        lateinit var instance: App
    }
}