package com.tapbi.spark.voiceai.ui.main.onboard

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.ads.screen.showAdsInterFinishOnboard
import com.tapbi.spark.voiceai.ads.screen.showAdsRewardFinishOnboard
import com.tapbi.spark.voiceai.common.models.Onboard
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import com.tapbi.spark.ui_common_sdk.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(private val navigation: MainNavigator) :
    BaseViewModel() {
    private val _uiState = MutableStateFlow(OnboardUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    fun getOnboard() {
        viewModelScope.launch(Dispatchers.IO) {
            val listOnBoard = listOf(
                Onboard(
                    R.drawable.onboarding_image_1,
                    R.string.easy_to_create_your_own_call
                ),
                Onboard(
                    R.drawable.onboarding_image_2,
                    R.string.enjoy_your_calling_experience
                ),
                Onboard(
                    R.drawable.onboarding_image_3,
                    R.string.flash_effect_on_incoming_call
                )
            )
            _uiState.update { it.copy(data = listOnBoard) }
        }
    }

    internal fun showAdsReward(isPro: Boolean, appCompatActivity: AppCompatActivity?) {
        showAdsRewardFinishOnboard(isPro, appCompatActivity, onFail = {
            appCompatActivity?.apply {
                showToast(getString(R.string.no_internet_connection_found_ncheck_your_connection))
            }
        }, onComplete = {
            onOnboardAdFlowReadyToContinue()
        })
    }

    internal fun showAdsInter(isPro: Boolean, appCompatActivity: AppCompatActivity?) {
        showAdsInterFinishOnboard(
            isPro,
            appCompatActivity
        ) { navigation.openHomeFromOnboard() }
    }

    internal fun onOnboardAdFlowReadyToContinue() {
        _uiState.update { it.copy(nextScreen = true) }
    }

    internal fun nextScreen() {
        if (uiState.value.nextScreen) {
            navigation.openHomeFromOnboard()
            _uiState.update { it.copy(nextScreen = false) }
        }
    }

    init {
        getOnboard()
    }
}
