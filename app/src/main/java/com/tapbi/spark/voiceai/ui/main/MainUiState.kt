package com.tapbi.spark.voiceai.ui.main

import com.tapbi.spark.ui_common_sdk.foundation.ThemeType
import java.util.Locale


data class MainUiState(
    val theme: ThemeType = ThemeType.USER_DEFAULT,
    val isPro: Boolean = false,
    val onPurchased: Boolean = false,
    val currentLocale: Locale = Locale.ENGLISH,
) {
    companion object {
        val EMPTY =
            MainUiState()
    }
}
