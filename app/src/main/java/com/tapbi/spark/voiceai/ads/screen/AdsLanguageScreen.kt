package com.tapbi.spark.voiceai.ads.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.tapbi.spark.voiceai.ads.feature.AdsUiState

@Composable
fun ViewAdsNativeLanguageScreen(appCompatActivity: AppCompatActivity?, adsUiState: AdsUiState) {
    // Commented out UI ads
    /*
    if (appCompatActivity == null || !adsUiState.showAds) return
    val nativeMap = remember(appCompatActivity) {
        Common.getMapIdAdmobApplovin(
            appCompatActivity,
            R.array.admob_native_language,
            R.array.applovin_native_language
        )
    }
    NativeAdNoMediaCompose(
        modifier = Modifier.fillMaxWidth(),
        activity = appCompatActivity,
        idNativeMap = nativeMap,
        templateLayoutId = R.layout.gnt_admob_native_layout_no_media_3,
        tag = stringResource(R.string.tag_native_language),
        remoteConfigReloadKey = adsUiState.reloadKey,
        onDecorationAds = object : OnDecorationAds {
            override fun onDecoration(network: String?) {
            }
        })
    */
}

@Composable
fun ViewAdsNativeFullLanguageScreen(
    appCompatActivity: AppCompatActivity?,
    adsUiState: AdsUiState,
    closeAds: () -> Unit
) {
    // Commented out UI ads
    /*
    if (appCompatActivity == null || !adsUiState.showAds) return
    val nativeMap = remember(appCompatActivity) {
        Common.getMapIdAdmobApplovin(
            appCompatActivity,
            R.array.admob_native_language,
            R.array.applovin_native_language
        )
    }
    NativeAdNoMediaCompose(
        modifier = Modifier.fillMaxSize(),
        activity = appCompatActivity,
        idNativeMap = nativeMap,
        templateLayoutId = R.layout.admob_native_layout_full,
        tag = stringResource(R.string.tag_native_language),
        remoteConfigReloadKey = adsUiState.reloadKey, onTemplateViewReady = {
            it.setOnClickCloseAdsListener(R.id.imgExit, {
                closeAds.invoke()
            })
        })
    */
}

@Composable
fun ViewAdsBannerLanguageScreen(appCompatActivity: AppCompatActivity?, adsUiState: AdsUiState) {
    // Commented out UI ads
    /*
    if (appCompatActivity == null) return
    val bannerMap = remember(appCompatActivity) {
        hashMapOf(
            Common.ADS_NET_WORK_ADMOB to appCompatActivity.resources.getResourceEntryName(R.string.admob_banner_language),
            Common.ADS_NET_WORK_APPLOVIN to appCompatActivity.resources.getResourceEntryName(R.string.applovin_banner_language)
        )
    }
    BannerAdCompose(
        activity = appCompatActivity,
        idBannerMap = bannerMap,
        modifier = Modifier.fillMaxWidth(),
        tag = stringResource(R.string.tag_banner_language),
        isCollapsible = true,
        showAds = adsUiState.showAds
    )
    */
}
