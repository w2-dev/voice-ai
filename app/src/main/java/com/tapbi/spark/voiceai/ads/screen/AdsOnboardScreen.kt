package com.tapbi.spark.voiceai.ads.screen

import androidx.appcompat.app.AppCompatActivity
import com.tapbi.spark.voiceai.ads.feature.showAdsFull
import com.tapbi.spark.voiceai.ads.feature.showAdsReward
import com.tapbi.spark.voiceai.R

fun showAdsInterFinishOnboard(
    isPro: Boolean,
    appCompatActivity: AppCompatActivity?,
    nextAfterAds: () -> Unit
) {
    if (isPro || appCompatActivity == null) {
        nextAfterAds()
        return
    }
    showAdsFull(
        positionAdsName = appCompatActivity.getString(R.string.tag_inter_finish_onboard),
        appCompatActivity = appCompatActivity,
        manualMode = false,
        nextAfterFullScreen = nextAfterAds
    )
}

fun showAdsRewardFinishOnboard(
    isPro: Boolean,
    appCompatActivity: AppCompatActivity?,
    onFail: () -> Unit,
    onComplete: () -> Unit
) {
    if (isPro || appCompatActivity == null) {
        onComplete()
        return
    }
    showAdsReward(
        tag = appCompatActivity.getString(R.string.tag_reward_finish_onboard),
        appCompatActivity = appCompatActivity,
        onFail = onFail,
        onComplete = onComplete
    )
}
