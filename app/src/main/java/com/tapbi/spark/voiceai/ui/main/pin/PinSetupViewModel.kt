package com.tapbi.spark.voiceai.ui.main.pin

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
class PinSetupViewModel @Inject constructor(
    private val navigation: MainNavigator,
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        PinUiState.INITIAL.copy(
            hasSavedPin = LockScreenState.hasPinSetup
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onKeyPadClick(digit: String) {
        val current = _uiState.value.currentInput
        if (current.length >= 4) return

        val newPin = current + digit
        _uiState.update { it.copy(currentInput = newPin, isError = false) }

        if (newPin.length == 4) {
            handlePinSubmitted(newPin)
        }
    }

    fun onDeleteClick() {
        val current = _uiState.value.currentInput
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(currentInput = current.dropLast(1), isError = false) }
        }
    }

    private fun handlePinSubmitted(pin: String) {
        val currentState = _uiState.value
        if (currentState.step == 1) {
            _uiState.update {
                it.copy(
                    step = 2,
                    firstPin = pin,
                    currentInput = "",
                    message = "Nhập lại mã PIN để xác nhận",
                    isError = false
                )
            }
        } else {
            if (pin == currentState.firstPin) {
                savePin(pin)
            } else {
                _uiState.update {
                    it.copy(
                        currentInput = "",
                        message = "Mã PIN không khớp! Vui lòng thử lại",
                        isError = true
                    )
                }
            }
        }
    }

    private fun savePin(pin: String) {
        LockScreenState.pinPassword = pin
        LockScreenState.isPinLockEnabled = true

        viewModelScope.launch {
            preferencesDataStoreHelper.putString(PrefKeys.PIN_PASSWORD, pin)
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, true)
        }

        _uiState.update {
            it.copy(
                message = "✅ Đã lưu mã PIN thành công!",
                isError = false,
                hasSavedPin = true
            )
        }
    }

    fun deletePin() {
        LockScreenState.pinPassword = ""
        LockScreenState.isPinLockEnabled = false

        viewModelScope.launch {
            preferencesDataStoreHelper.putString(PrefKeys.PIN_PASSWORD, "")
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, false)
        }

        _uiState.update {
            PinUiState(
                message = "Đã xóa mã PIN",
                hasSavedPin = false
            )
        }
    }

    fun resetSetup() {
        _uiState.update {
            PinUiState(
                step = 1,
                message = "Nhập mã PIN 4 chữ số",
                hasSavedPin = LockScreenState.hasPinSetup
            )
        }
    }

    fun popBackStack() {
        navigation.popBackStack()
    }

    fun openPremium() {
        navigation.navigateTo(NavigationScreen.PremiumScreen.route)
    }
}
