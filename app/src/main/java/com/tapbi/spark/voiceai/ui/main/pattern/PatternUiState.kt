package com.tapbi.spark.voiceai.ui.main.pattern

import com.tapbi.spark.voiceai.common.models.LockScreenState

enum class PatternSetupStep {
    CHOOSE_TYPE,
    FIRST_DRAW,
    CONFIRM_DRAW,
    SUCCESS
}

data class PatternGridType(
    val rows: Int,
    val cols: Int,
    val title: String,
    val samplePattern: List<Int>
)

data class PatternUiState(
    val step: PatternSetupStep = PatternSetupStep.CHOOSE_TYPE,
    val selectedRows: Int = LockScreenState.patternRows,
    val selectedCols: Int = LockScreenState.patternCols,
    val message: String = "Chọn kiểu lưới hình vẽ của bạn",
    val isError: Boolean = false,
    val errorCount: Int = 0,
    val firstPattern: List<Int>? = null,
    val hasSavedPattern: Boolean = LockScreenState.hasPatternSetup
) {
    companion object {
        val EMPTY = PatternUiState()

        val AVAILABLE_TYPES = listOf(
            PatternGridType(2, 3, "2 * 3", listOf(0, 1, 4, 3)),
            PatternGridType(4, 4, "4 * 4", listOf(1, 5, 9, 13, 12)),
            PatternGridType(3, 3, "3 * 3", listOf(1, 2, 5, 4, 7)),
            PatternGridType(5, 5, "5 * 5", listOf(0, 1, 6, 12, 18, 24))
        )
    }
}
