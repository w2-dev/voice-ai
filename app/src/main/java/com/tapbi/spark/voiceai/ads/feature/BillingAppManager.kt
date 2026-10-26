package com.tapbi.spark.voiceai.ads.feature

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.android.billingclient.api.BillingClient
import com.ironman.spark.billing.BillingManager
import com.ironman.spark.billing.BillingManager.findBillingModel
import com.ironman.spark.billing.BillingModel
import com.tapbi.spark.voiceai.R

object BillingAppManager {
    private var listSub: Array<String>? = null
    fun init(activity: Activity?) {
        if (listSub == null && activity != null) {
            listSub = activity.resources.getStringArray(R.array.subscription_list)
        }
    }

    fun onPurchaseMonth(activity: AppCompatActivity?) {
        if (activity == null || listSub == null) return
        val billing = findBillingModel(listSub!![0], BillingClient.ProductType.SUBS)
        onPurchase(billing, activity)
    }

    fun onPurchaseYear(activity: AppCompatActivity?) {
        if (activity == null || listSub == null) return
        val billing = findBillingModel(listSub!![1], BillingClient.ProductType.SUBS)
        onPurchase(billing, activity)
    }

    fun onPurchaseLifeTime(activity: AppCompatActivity?) {
        if (activity == null) return
        val billing = findBillingModel(
            activity.resources.getString(R.string.purchase_pro_id_permanently),
            BillingClient.ProductType.INAPP
        )
        onPurchase(billing, activity)
    }

    fun onPurchase(billingSelected: BillingModel?, activity: AppCompatActivity?) {
        if (activity == null) return
        if (billingSelected == null) {
            Toast.makeText(
                activity,
                activity.getString(com.ironman.spark.billing.R.string.billing_error_connection),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            BillingManager.purchaseBilling(
                activity,
                billingSelected,
                activity as BillingManager.OnBillingListener
            )
        }
    }

    fun getPriceIdMonth(): String{
        return listSub?.get(0)?:""
    }

    fun getPriceIdYear(): String{
        return listSub?.get(1)?:""
    }

     fun setUpBilling(lifecycle: Lifecycle) {
        BillingManager.setupBillingClient()
        lifecycle.addObserver(BillingManager)
    }

    fun initResource(application: Application,){
        BillingManager.init(
            application,
            null,
            listOf( application.getString(R.string.purchase_pro_id_permanently)),
            listOf(*application.resources.getStringArray(R.array.subscription_list)),
            "REMOVE_ADS"
        )
    }
}