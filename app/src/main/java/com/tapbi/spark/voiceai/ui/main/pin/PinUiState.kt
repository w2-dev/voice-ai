package com.tapbi.spark.voiceai.ui.main.pin

data class PinUiState(
    val step: Int = 1,
    val firstPin: String = "",
    val currentInput: String = "",
    val message: String = "Nhập mã PIN 4 chữ số",
    val isError: Boolean = false,
    val hasSavedPin: Boolean = false
) {
    companion object {
        val INITIAL = PinUiState()
    }
}
