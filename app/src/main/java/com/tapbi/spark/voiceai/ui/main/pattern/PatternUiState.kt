package com.tapbi.spark.voiceai.ui.main.pattern

import com.tapbi.spark.voiceai.common.models.LockScreenState

enum class PatternStep {
    FIRST_DRAW,
    CONFIRM_DRAW,
    SUCCESS
}

data class PatternUiState(
    val step: PatternStep = PatternStep.FIRST_DRAW,
    val message: String = "Vẽ hình mẫu của bạn (Nối ít nhất 4 điểm)",
    val isError: Boolean = false,
    val firstPattern: List<Int>? = null,
    val hasSavedPattern: Boolean = LockScreenState.hasPatternSetup
) {
    companion object {
        val EMPTY = PatternUiState()
    }
}
