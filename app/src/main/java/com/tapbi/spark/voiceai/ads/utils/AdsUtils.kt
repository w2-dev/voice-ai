package com.tapbi.spark.voiceai.ads.utils

import com.ironman.trueads.admob.ControlAds.admobInitialized
import com.ironman.trueads.common.Common

fun checkAdsNativeIsDisable(tag: String): Boolean {
    return Common.checkAdsIsDisable(tag, Common.TYPE_ADS_NATIVE)
}

fun isAdmobInitialized(): Boolean {
    return admobInitialized
}