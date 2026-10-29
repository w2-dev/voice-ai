package com.tapbi.spark.voiceai.ui.main.premium

import com.tapbi.spark.ui_common_sdk.components.ContentState

enum class PlanType {
    MONTHLY,
    YEARLY,
    LIFETIME
}

data class PremiumUiState(
    val priceMonthly: String = "$2.99 / Month",
    val priceYearly: String = "$19.99 / Year",
    val priceLifeTime: String = "$49.99 Lifetime",
    val selectedPlan: PlanType = PlanType.YEARLY,
    val contentState: ContentState = ContentState.LOADED,
) {
    companion object {
        val EMPTY = PremiumUiState()
    }
}
