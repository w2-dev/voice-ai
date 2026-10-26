package com.tapbi.spark.voiceai.ui.main.language

import com.tapbi.spark.voiceai.ads.feature.AdsUiState
import com.tapbi.spark.voiceai.common.models.Language
import com.tapbi.spark.ui_common_sdk.components.ContentState


data class LanguageUiState(
    val contentState: ContentState,
    val data: List<Language>,
    val selectedLocale: String = "",
    val isFromSplash: Boolean = true,
    val adsUiState: AdsUiState = AdsUiState(false, "")
) {
    companion object {
        val EMPTY =
            LanguageUiState(
                contentState = ContentState.LOADING,
                data = emptyList(),
                selectedLocale = ""
            )
    }
}
