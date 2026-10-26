package com.tapbi.spark.voiceai.ads.feature

import androidx.appcompat.app.AppCompatActivity
import com.ironman.spark.billing.BillingManager
import com.ironman.trueads.common.Common
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.voiceai.R

fun autoLoadsAds(activity: AppCompatActivity) {
    if (BillingManager.isPremium) return
    autoloadAdsNative(
        R.array.admob_native_language,
        R.array.applovin_native_language,
        listOf(activity.getString(R.string.tag_native_language)), activity
    )
    MultiAdsControl.loadAdsInterstitialDetectInternet(activity.applicationContext, activity)
    MultiAdsControl.loadAdsRewardDetectInternet(activity.applicationContext, activity)
}

private fun autoloadAdsNative(
    idAdmob: Int,
    idApplovin: Int,
    tag: List<String>?,
    activity: AppCompatActivity
) {
    val hashMapNativeOnBoarding =
        Common.getMapIdAdmobApplovin(activity, idAdmob, idApplovin)
    MultiAdsControl.loadAdsNativeDetectInternet(
        activity,
        tag,
        hashMapNativeOnBoarding
    )
}
