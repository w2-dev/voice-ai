package com.tapbi.spark.voiceai.ui.main.security

import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityQuestionViewModel @Inject constructor(
    private val navigation: MainNavigator,
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        SecurityQuestionUiState(
            selectedIndex = 0,
            answerInput = ""
        )
    )
    val uiState = _uiState.asStateFlow()

    fun resetState() {
        _uiState.update {
            it.copy(
                selectedIndex = if (it.isUnlockMode) LockScreenState.securityQuestionIndex else 0,
                answerInput = "",
                isError = false,
                isSuccess = false,
                message = if (it.isUnlockMode) "Nhập câu trả lời để xác thực mở khóa" else "Thiết lập câu hỏi & câu trả lời bảo mật"
            )
        }
    }

    fun setUnlockMode(isUnlock: Boolean) {
        _uiState.update {
            it.copy(
                isUnlockMode = isUnlock,
                selectedIndex = if (isUnlock) LockScreenState.securityQuestionIndex else 0,
                answerInput = "",
                isError = false,
                isSuccess = false,
                message = if (isUnlock) "Nhập câu trả lời để xác thực mở khóa" else "Thiết lập câu hỏi & câu trả lời bảo mật"
            )
        }
    }

    fun onQuestionSelected(index: Int) {
        _uiState.update { it.copy(selectedIndex = index, isError = false) }
    }

    fun onAnswerChanged(answer: String) {
        _uiState.update { it.copy(answerInput = answer, isError = false) }
    }

    fun onConfirmClick(onSuccessUnlock: (() -> Unit)? = null) {
        val state = _uiState.value
        val trimmedAnswer = state.answerInput.trim()

        if (trimmedAnswer.isBlank()) {
            _uiState.update {
                it.copy(
                    message = "Vui lòng nhập câu trả lời!",
                    isError = true
                )
            }
            return
        }

        if (state.isUnlockMode) {
            if (LockScreenState.hasSecurityQuestionSetup) {
                val isQuestionMatch = state.selectedIndex == LockScreenState.securityQuestionIndex
                val isAnswerMatch = trimmedAnswer.equals(LockScreenState.securityAnswer, ignoreCase = true)

                if (isQuestionMatch && isAnswerMatch) {
                    _uiState.update {
                        it.copy(
                            message = "✅ Xác thực thành công! Đang mở khóa...",
                            isError = false,
                            isSuccess = true
                        )
                    }
                    onSuccessUnlock?.invoke()
                } else if (!isQuestionMatch) {
                    _uiState.update {
                        it.copy(
                            message = "❌ Không đúng câu hỏi bảo mật đã cài đặt!",
                            isError = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            message = "❌ Câu trả lời không đúng! Vui lòng thử lại.",
                            isError = true
                        )
                    }
                }
            } else {
                saveSecurityQuestion(state.selectedIndex, trimmedAnswer)
                _uiState.update {
                    it.copy(
                        message = "✅ Đã lưu câu hỏi & xác thực mở khóa thành công!",
                        isError = false,
                        isSuccess = true
                    )
                }
                onSuccessUnlock?.invoke()
            }
        } else {
            saveSecurityQuestion(state.selectedIndex, trimmedAnswer)
            _uiState.update {
                it.copy(
                    message = "✅ Đã lưu câu hỏi bảo mật thành công!",
                    isError = false,
                    isSuccess = true
                )
            }
            onSuccessUnlock?.invoke() ?: popBackStack()
        }
    }

    private fun saveSecurityQuestion(index: Int, answer: String) {
        LockScreenState.securityQuestionIndex = index
        LockScreenState.securityAnswer = answer

        com.tapbi.spark.voiceai.data.preferences.ServicePreferences.saveSecurityQuestion(
            com.tapbi.spark.voiceai.App.instance,
            index,
            answer
        )
        viewModelScope.launch {
            preferencesDataStoreHelper.putInt(PrefKeys.SECURITY_QUESTION_INDEX, index)
            preferencesDataStoreHelper.putString(PrefKeys.SECURITY_ANSWER, answer)
        }
    }

    fun popBackStack() {
        navigation.popBackStack()
    }

    fun openPremium() {
        navigation.navigateTo(NavigationScreen.PremiumScreen.route)
    }
}
