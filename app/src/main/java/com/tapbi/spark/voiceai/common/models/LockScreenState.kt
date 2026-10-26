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

    val hasVoiceSetup: Boolean get() = voicePassphrase.isNotBlank() || referenceEmbedding != null
    val hasPinSetup: Boolean get() = pinPassword.isNotBlank()
    val hasPatternSetup: Boolean get() = !targetPattern.isNullOrEmpty()
}
