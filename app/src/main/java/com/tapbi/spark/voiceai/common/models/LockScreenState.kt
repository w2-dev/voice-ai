package com.tapbi.spark.voiceai.common.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LockScreenState {
    var isLockScreenActive by mutableStateOf(false)

    var isVoiceLockEnabled by mutableStateOf(false)
    var voicePassphrase by mutableStateOf("")
    var referenceEmbedding by mutableStateOf<FloatArray?>(null)
    var speechLanguageCode by mutableStateOf("vi-VN")

    var isPinLockEnabled by mutableStateOf(false)
    var pinPassword by mutableStateOf("")

    var isPatternLockEnabled by mutableStateOf(false)
    var patternRows by mutableIntStateOf(3)
    var patternCols by mutableIntStateOf(3)
    var targetPattern by mutableStateOf<List<Int>?>(null)

    var isFingerprintEnabled by mutableStateOf(false)

    var securityQuestionIndex by mutableIntStateOf(0)
    var securityAnswer by mutableStateOf("")

    val SAMPLE_SECURITY_QUESTIONS = listOf(
        "Món ăn bạn yêu thích nhất là gì?",
        "Tên con vật nuôi đầu tiên của bạn là gì?",
        "Thành phố nơi bạn sinh ra là gì?",
        "Tên trường tiểu học của bạn là gì?",
        "Ca sĩ hoặc ban nhạc bạn yêu thích nhất là gì?"
    )

    val currentSecurityQuestion: String
        get() = SAMPLE_SECURITY_QUESTIONS.getOrElse(securityQuestionIndex) { SAMPLE_SECURITY_QUESTIONS[0] }

    val hasVoiceSetup: Boolean get() = voicePassphrase.isNotBlank() || referenceEmbedding != null
    val hasPinSetup: Boolean get() = pinPassword.isNotBlank()
    val hasPatternSetup: Boolean get() = !targetPattern.isNullOrEmpty()
    val hasSecurityQuestionSetup: Boolean get() = securityAnswer.isNotBlank()
}
