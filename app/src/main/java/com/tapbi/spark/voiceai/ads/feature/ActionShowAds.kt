package com.tapbi.spark.voiceai.ads.feature

import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxReward
import com.ironman.trueads.multiads.InterstitialAdsListener
import com.ironman.trueads.multiads.MultiAdsControl
import com.ironman.trueads.multiads.RewardAdsListener
import com.ironman.trueads.multiads.ShowOpenAdsListener
import timber.log.Timber

fun showAdsOpen(appCompatActivity: AppCompatActivity, nextAfterAds: () -> Unit) {
    MultiAdsControl.setConfigForAdsOpenSplash(appCompatActivity)
    MultiAdsControl.loadAndShowOpenAds(
        appCompatActivity,
        false,
        object : ShowOpenAdsListener {
            override fun onPrepareShowAdsOpenApp() {}

            override fun onLoadedAdsOpenApp() {}

            override fun onLoadFailAdsOpenApp() {
                nextAfterAds()
            }

            override fun onShowAdsOpenAppDismissed() {
                nextAfterAds()
            }

            override fun onAdsOpenLoadedButNotShow() {
                nextAfterAds()
            }

            override fun onAdsOpenClicked() {}

            override fun onAdRevenuePaid(ad: MaxAd?) {}
        }
    )
}

fun showAdsFull(
    positionAdsName: String,
    appCompatActivity: AppCompatActivity,
    manualMode: Boolean = false,
    nextAfterFullScreen: () -> Unit
) {
    showAdsFullWaiting(positionAdsName, appCompatActivity, manualMode, false, nextAfterFullScreen) {}
}

fun showAdsFullWaiting(
    positionAdsName: String,
    appCompatActivity: AppCompatActivity,
    manualMode: Boolean = false,
    waitingShow: Boolean = false,
    nextAfterFullScreen: () -> Unit,
    nextAfterShowFail: () -> Unit
) {
    Timber.e("HaiPd showAdsFull: $positionAdsName")
    MultiAdsControl.showInterstitial(
        appCompatActivity,
        positionAdsName,
        manualMode = manualMode,
        waitingForShow = waitingShow,
        showInterstitialAdsListener = object : InterstitialAdsListener {
            override fun onInterstitialAdsClicked(adsType: Int) {}

            override fun onInterstitialAdsClose(adsType: Int) {
                nextAfterFullScreen()
            }

            override fun onInterstitialAdsLoadFailed(adsType: Int) {
                nextAfterShowFail()
            }

            override fun onInterstitialAdsLoaded(adsType: Int) {}

            override fun onInterstitialAdsNotShow(adsType: Int) {
                nextAfterFullScreen()
            }

            override fun onInterstitialAdsShowFailed(adsType: Int) {
                nextAfterShowFail()
            }

            override fun onInterstitialAdsShowFully(adsType: Int) {}

            override fun onPrepareShowInterstitialAds(adsType: Int) {}

            override fun onAdRevenuePaid(ad: MaxAd) {}
        }
    )
}

fun showAdsReward(
    tag: String,
    appCompatActivity: AppCompatActivity,
    onFail: () -> Unit,
    onComplete: () -> Unit
) {
    Timber.e("HaiPd showAdsReward: $tag")
    MultiAdsControl.showReward(
        appCompatActivity,
        tag,
        object : RewardAdsListener {
            override fun onAdsRewarded(
                adsType: Int,
                maxReward: MaxReward?,
                rewardItem: Int
            ) {
                onComplete()
            }

            override fun onRewardAdsClose(adsType: Int) {
                onComplete()
            }

            override fun onRewardAdsShowFailed(adsType: Int) {
                onFail()
            }

            override fun onPrepareShowRewardAds(adsType: Int) {}

            override fun onRewardAdsClicked(adsType: Int) {}

            override fun onAdRevenuePaid(ad: MaxAd) {}
        }
    )
}
