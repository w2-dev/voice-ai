package com.tapbi.spark.voiceai.ads.feature

data class AdsUiState(val showAds: Boolean, val reloadKey: String/*(For items in lazy columns)*/)