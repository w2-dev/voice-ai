package com.tapbi.spark.voiceai.ui.main.onboard

import com.tapbi.spark.voiceai.common.models.Onboard


data class OnboardUiState(
    val nextScreen: Boolean = false,
    val data: List<Onboard>
) {
    companion object {
        val EMPTY =
            OnboardUiState(data = listOf())

    }
}
