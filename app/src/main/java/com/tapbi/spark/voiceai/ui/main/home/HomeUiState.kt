package com.tapbi.spark.voiceai.ui.main.home

data class HomeUiState(
    val isLockScreenActive: Boolean = false,
    val isVoiceLockEnabled: Boolean = false,
    val isPinLockEnabled: Boolean = false,
    val isPatternLockEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val showFingerprintSetupDialog: Boolean = false,
    val isAiProtectionUpgraded: Boolean = false
) {
    companion object {
        val EMPTY = HomeUiState()
    }
}
