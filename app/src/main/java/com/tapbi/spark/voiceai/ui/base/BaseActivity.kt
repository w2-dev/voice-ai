package com.tapbi.spark.voiceai.ui.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.ironman.spark.billing.PremiumLiveData
import com.tapbi.spark.voiceai.ads.feature.BillingAppManager

abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {
    lateinit var viewModel: VM
    var isPro = false
    abstract fun provideViewModel(): Class<VM>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenOrientation()
        viewModel = ViewModelProvider(this)[provideViewModel()]
        initBilling()
        PremiumLiveData.get().observe(this) { aBoolean ->
            if (aBoolean != null) {
                if (isPro != aBoolean) {
                    isPro = aBoolean
                    updateWhenBecomePro(aBoolean)
                }
            }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        hideSystemBars()
        setupView(savedInstanceState)
    }

    fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    abstract fun setupView(savedInstanceState: Bundle?)

    private fun initBilling() {
        BillingAppManager.init(this)
        BillingAppManager.setUpBilling(lifecycle)
    }

    open fun updateWhenBecomePro(isPro: Boolean) {
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun lockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

}
