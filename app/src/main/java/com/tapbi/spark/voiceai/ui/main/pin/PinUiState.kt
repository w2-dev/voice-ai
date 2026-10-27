package com.tapbi.spark.voiceai.ui.main.pin

import com.tapbi.spark.voiceai.common.models.LockScreenState

data class PinUiState(
    val step: Int = 1,
    val firstPin: String = "",
    val currentInput: String = "",
    val message: String = "Nhập mã PIN 4 chữ số (1/2)",
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val hasSavedPin: Boolean = LockScreenState.hasPinSetup
) {
    companion object {
        val INITIAL = PinUiState()
    }
}
