package com.tapbi.spark.voiceai.ui.main.splash

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.ironman.spark.billing.BillingManager
import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.data.preferences.ServicePreferences
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val navigation: MainNavigator,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    internal fun openPremiumFromSplash() {
        navigation.openPremiumFromSplash()
    }

    internal fun startSplashTimer() {
        viewModelScope.launch {
            delay(3000L.milliseconds)
            val context = App.instance
            ServicePreferences.setSplashCompleted(context, true)
            val isFirst = ServicePreferences.isFirstLaunch(context)
            if (isFirst) {
                navigation.openLanguageFromSplash()
            } else {
                navigation.openHomeFromSplash()
            }
        }
    }

    internal fun initConsent(activity: Activity?) {
        startSplashTimer()
    }

    fun showAds(activity: AppCompatActivity) {
        startSplashTimer()
    }

    init {
        BillingManager.refreshPurchase(
            needShowMessage = false, forceRefresh = false, onBillingListener = null
        )
    }
}
