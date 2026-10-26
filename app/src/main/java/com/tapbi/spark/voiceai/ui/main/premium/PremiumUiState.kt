package com.tapbi.spark.voiceai.ui.main.premium

import com.tapbi.spark.ui_common_sdk.components.ContentState


data class PremiumUiState(
    val priceMonthly: String = "",
    val priceYearly: String = "",
    val priceLifeTime: String = "",
    val contentState: ContentState = ContentState.ERROR,
) {
    companion object {
        val EMPTY =
            PremiumUiState()

    }
}
