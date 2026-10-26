package com.tapbi.spark.voiceai.common.models

sealed class EnrollmentResult {
    data class StepSuccess(val step: Int, val passphrase: String, val message: String) : EnrollmentResult()
    data class MismatchError(val currentStep: Int, val expectedPassphrase: String, val message: String) : EnrollmentResult()
    data class CompleteSuccess(val passphrase: String, val message: String) : EnrollmentResult()
}
