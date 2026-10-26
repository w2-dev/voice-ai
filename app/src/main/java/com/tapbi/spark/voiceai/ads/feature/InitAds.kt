package com.tapbi.spark.voiceai.ads.feature

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.voiceai.App

fun initWaitingBillingAndRemoteConfig(
    activity: AppCompatActivity,
    application: App,
    nextScreen: (() -> Unit)? = null,
    loadAdsOpen: (() -> Unit)? = null
) {
    nextScreen?.invoke()
}

fun enableShowOpenForeground(enable: Boolean, context: Context) {
    (context.applicationContext as? Application)?.let { app ->
        MultiAdsControl.enableShowAdsOpenForeground(app, enable)
    }
}
