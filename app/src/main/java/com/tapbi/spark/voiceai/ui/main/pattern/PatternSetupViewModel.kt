package com.tapbi.spark.voiceai.ui.main.pattern

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
class PatternSetupViewModel @Inject constructor(
    private val navigation: MainNavigator,
    private val preferencesDataStoreHelper: PreferencesDataStoreHelper
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(PatternUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    fun selectGridType(rows: Int, cols: Int) {
        LockScreenState.patternRows = rows
        LockScreenState.patternCols = cols
        _uiState.update {
            it.copy(
                selectedRows = rows,
                selectedCols = cols,
                isError = false
            )
        }
    }

    fun applyGridType() {
        _uiState.update {
            it.copy(
                step = PatternSetupStep.FIRST_DRAW,
                message = "Vẽ hình mẫu của bạn (Nối ít nhất 4 điểm)",
                isError = false
            )
        }
        navigation.openPatternSetup()
    }

    fun onPatternDrawn(pattern: List<Int>) {
        if (pattern.size < 4) {
            _uiState.update {
                it.copy(
                    message = "Hình vẽ quá ngắn. Vẽ ít nhất 4 điểm.",
                    isError = true
                )
            }
            return
        }

        when (_uiState.value.step) {
            PatternSetupStep.CHOOSE_TYPE -> {
                applyGridType()
            }

            PatternSetupStep.FIRST_DRAW -> {
                _uiState.update {
                    it.copy(
                        step = PatternSetupStep.CONFIRM_DRAW,
                        firstPattern = pattern,
                        message = "Vẽ lại hình mẫu để xác nhận",
                        isError = false
                    )
                }
            }

            PatternSetupStep.CONFIRM_DRAW -> {
                if (pattern == _uiState.value.firstPattern) {
                    val rows = _uiState.value.selectedRows
                    val cols = _uiState.value.selectedCols
                    LockScreenState.patternRows = rows
                    LockScreenState.patternCols = cols
                    LockScreenState.targetPattern = pattern
                    LockScreenState.isPatternLockEnabled = true

                    viewModelScope.launch {
                        preferencesDataStoreHelper.putInt(PrefKeys.PATTERN_GRID_ROWS, rows)
                        preferencesDataStoreHelper.putInt(PrefKeys.PATTERN_GRID_COLS, cols)
                        preferencesDataStoreHelper.putString(
                            PrefKeys.PATTERN_LOCK_DATA,
                            pattern.joinToString(",")
                        )
                        preferencesDataStoreHelper.putBoolean(
                            PrefKeys.IS_PATTERN_LOCK_ENABLED,
                            true
                        )
                    }
                    _uiState.update {
                        it.copy(
                            step = PatternSetupStep.SUCCESS,
                            message = "Cài đặt hình vẽ thành công!",
                            isError = false,
                            hasSavedPattern = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            step = PatternSetupStep.FIRST_DRAW,
                            firstPattern = null,
                            message = "Hình vẽ không khớp! Vui lòng thử lại từ đầu.",
                            isError = true
                        )
                    }
                }
            }

            PatternSetupStep.SUCCESS -> {
                // Completed
            }
        }
    }

    fun resetSetupState() {
        _uiState.update {
            it.copy(
                step = PatternSetupStep.CHOOSE_TYPE,
                firstPattern = null,
                message = "Chọn kiểu lưới hình vẽ của bạn",
                isError = false,
                hasSavedPattern = LockScreenState.hasPatternSetup
            )
        }
    }

    fun resetSetup() {
        resetSetupState()
        navigation.popBackStack()
    }

    fun deletePattern() {
        LockScreenState.targetPattern = null
        LockScreenState.isPatternLockEnabled = false
        viewModelScope.launch {
            preferencesDataStoreHelper.removeKey(PrefKeys.PATTERN_LOCK_DATA)
            preferencesDataStoreHelper.putBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, false)
        }
        _uiState.update {
            it.copy(
                step = PatternSetupStep.CHOOSE_TYPE,
                firstPattern = null,
                message = "Đã xóa mật khẩu hình vẽ",
                isError = false,
                hasSavedPattern = false
            )
        }
        navigateToHome()
    }

    fun navigateToHome() {
        navigation.navigateTo(NavigationScreen.HomeScreen.route)
    }

    fun openPremium() {
        navigation.navigateTo(NavigationScreen.PremiumScreen.route)
    }

    fun popBackStack() {
        navigation.popBackStack()
    }
}
