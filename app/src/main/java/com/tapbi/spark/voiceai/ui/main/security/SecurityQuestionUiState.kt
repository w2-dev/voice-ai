package com.tapbi.spark.voiceai.ui.main.security

import com.tapbi.spark.voiceai.common.models.LockScreenState

data class SecurityQuestionUiState(
    val selectedIndex: Int = LockScreenState.securityQuestionIndex,
    val answerInput: String = LockScreenState.securityAnswer,
    val isUnlockMode: Boolean = false,
    val message: String = "",
    val isError: Boolean = false,
    val isSuccess: Boolean = false
) {
    val questions: List<String> get() = LockScreenState.SAMPLE_SECURITY_QUESTIONS
    val currentQuestion: String get() = questions.getOrElse(selectedIndex) { questions[0] }
}
