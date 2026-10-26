package com.tapbi.spark.voiceai.extension

import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.ads.feature.initWaitingBillingAndRemoteConfig
import com.tapbi.spark.voiceai.ui.main.MainActivity

fun MainActivity.initAdsAndLoadAds(
    nextScreen: (() -> Unit)? = null,
    loadAdsOpen: (() -> Unit)? = null
) {
    runOnUiThread {
        initWaitingBillingAndRemoteConfig(
            this,
            App.instance,
            nextScreen,
            loadAdsOpen,
        )
    }
}
