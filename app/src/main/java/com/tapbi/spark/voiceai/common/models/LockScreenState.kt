package com.tapbi.spark.voiceai.common.models

object LockScreenState {
    var isLockScreenActive: Boolean = false

    var isVoiceLockEnabled: Boolean = false
    var voicePassphrase: String = ""
    var referenceEmbedding: FloatArray? = null
    var speechLanguageCode: String = "vi-VN"

    var isPinLockEnabled: Boolean = false
    var pinPassword: String = ""

    var isPatternLockEnabled: Boolean = false
    var targetPattern: List<Int>? = null

    var isFingerprintEnabled: Boolean = false

    var securityQuestionIndex: Int = 0
    var securityAnswer: String = ""

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
