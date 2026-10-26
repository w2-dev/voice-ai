package com.tapbi.spark.voiceai.ui.main.splash


data class SplashUiState(
    val isVideoEnd: Boolean = false,
    val splashVisible: Boolean = false
) {
    companion object {
        val EMPTY =
            SplashUiState()

    }
}
