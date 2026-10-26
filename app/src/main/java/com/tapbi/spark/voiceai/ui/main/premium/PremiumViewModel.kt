package com.tapbi.spark.voiceai.ui.main.premium

import androidx.appcompat.app.AppCompatActivity
import com.ironman.spark.billing.BillingManager
import com.ironman.spark.billing.BillingModel
import com.tapbi.spark.voiceai.ads.feature.BillingAppManager
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import com.tapbi.spark.voiceai.ui.main.MainActivity
import com.tapbi.spark.ui_common_sdk.components.ContentState
import com.tapbi.spark.ui_common_sdk.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(private val navigator: MainNavigator) :
    BaseViewModel() {
    private val _uiState = MutableStateFlow(PremiumUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    fun updatePrice(billingModels: List<BillingModel>, activity: AppCompatActivity?) {
        if (activity == null) return
        var month = ""
        var year = ""
        var lifeTime = ""
        if (billingModels.isEmpty()) {
            _uiState.update {
                it.copy(
                    contentState = ContentState.ERROR
                )
            }
            return
        }
        billingModels.forEach { model ->
            val prices = BillingManager.getPriceForDisplay(
                activity,
                com.ironman.spark.billing.R.string.free_trial_then, model
            )
            var stringPrice = ""
            prices.forEach {
                stringPrice = stringPrice.plus(" ").plus(it)

            }
            when (model.productId) {
                BillingAppManager.getPriceIdMonth() -> month = stringPrice

                BillingAppManager.getPriceIdYear() -> year = stringPrice

                activity.resources.getString(R.string.purchase_pro_id_permanently) -> lifeTime =
                    model.pricesOneTime ?: ""
            }
        }
        _uiState.update {
            it.copy(
                priceMonthly = month,
                priceYearly = year,
                priceLifeTime = lifeTime,
                contentState = ContentState.LOADED
            )
        }
    }

    fun onPurchaseMonth(activity: AppCompatActivity?) {
        BillingAppManager.onPurchaseMonth(activity)
    }

    fun onPurchaseYear(activity: AppCompatActivity?) {
        BillingAppManager.onPurchaseYear(activity)
    }

    fun onPurchaseLifeTime(activity: AppCompatActivity?) {
        BillingAppManager.onPurchaseLifeTime(activity)
    }

    fun onClickRetry(activity: AppCompatActivity?) {
        if (activity == null) return
        if (BillingManager.checkBillingAvailable()) {
            BillingManager.refreshPurchase(
                needShowMessage = false,
                forceRefresh = false,
                onBillingListener = activity as MainActivity
            )
        } else {
            activity.showToast(activity.getString(com.ironman.spark.billing.R.string.billing_error_connection))
            BillingManager.retryBillingServiceIfNeeded(activity as MainActivity)
        }
    }

    fun popBackStack() {
        navigator.popBackStack()
    }
}