package com.tapbi.spark.voiceai.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ironman.spark.billing.BillingManager
import com.ironman.trueads.admob.ControlAds
import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.ads.feature.enableShowOpenForeground
import com.tapbi.spark.voiceai.ads.utils.isAdmobInitialized
import com.tapbi.spark.voiceai.common.models.LockScreenState
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.initAdsAndLoadAds
import com.tapbi.spark.voiceai.extension.isAtLeastApi26
import com.tapbi.spark.voiceai.extension.safeNavigate
import com.tapbi.spark.voiceai.navigation.Navigation
import com.tapbi.spark.voiceai.navigation.NavigationScreen
import com.tapbi.spark.voiceai.navigation.utils.NavTransaction
import com.tapbi.spark.voiceai.navigation.utils.RouteNavigation
import com.tapbi.spark.voiceai.navigation.utils.ScreenRoute
import com.tapbi.spark.voiceai.providers.LocalIsPro
import com.tapbi.spark.voiceai.service.LockScreenService
import com.tapbi.spark.voiceai.ui.base.BaseActivity
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel>(), BillingManager.OnBillingListener {
    @Inject
    lateinit var routeNavigation: RouteNavigation

    @Inject
    lateinit var preferencesDataStoreHelper: PreferencesDataStoreHelper

    private lateinit var navController: NavHostController

    override fun provideViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun updateWhenBecomePro(isPro: Boolean) {
        super.updateWhenBecomePro(isPro)
        Timber.e("HaiPd updateWhenBecomePro: $isPro")
        viewModel.updatePremium(isPro)
        enableShowOpenForeground(!isPro, App.instance)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val isActive = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_LOCK_SCREEN_ACTIVE, false)
            val isVoice = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_VOICE_LOCK_ENABLED, false)
            val voicePass = preferencesDataStoreHelper.getString(PrefKeys.VOICE_PASSPHRASE, "")
            val speechLang = preferencesDataStoreHelper.getString(PrefKeys.SELECTED_SPEECH_LANGUAGE, "vi-VN")
            val isPin = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PIN_LOCK_ENABLED, false)
            val pinPass = preferencesDataStoreHelper.getString(PrefKeys.PIN_PASSWORD, "")
            val isPattern = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_PATTERN_LOCK_ENABLED, false)
            val patternStr = preferencesDataStoreHelper.getString(PrefKeys.PATTERN_LOCK_DATA, "")
            val isFingerprint = preferencesDataStoreHelper.getBoolean(PrefKeys.IS_FINGERPRINT_ENABLED, false)
            val qIndex = preferencesDataStoreHelper.getInt(PrefKeys.SECURITY_QUESTION_INDEX, 0)
            val qAnswer = preferencesDataStoreHelper.getString(PrefKeys.SECURITY_ANSWER, "")

            LockScreenState.isLockScreenActive = isActive
            LockScreenState.isVoiceLockEnabled = isVoice
            LockScreenState.voicePassphrase = voicePass
            LockScreenState.speechLanguageCode = speechLang
            LockScreenState.isPinLockEnabled = isPin
            LockScreenState.pinPassword = pinPass
            LockScreenState.isPatternLockEnabled = isPattern
            if (patternStr.isNotBlank()) {
                LockScreenState.targetPattern = patternStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            }
            LockScreenState.isFingerprintEnabled = isFingerprint
            LockScreenState.securityQuestionIndex = qIndex
            LockScreenState.securityAnswer = qAnswer

            if (isActive && hasOverlayPermission()) {
                val intent = Intent(this@MainActivity, LockScreenService::class.java)
                if (isAtLeastApi26) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.updatePremium(isPro)
        routeNavigation.setScreenRoute(ScreenRoute.HOME)
        setContent {
            navController = rememberNavController()
            val context = LocalContext.current
            val rs = LocalResources.current
            val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
            val resources = remember(mainUiState.currentLocale) {
                val config = Configuration(rs.configuration).apply {
                    setLocale(mainUiState.currentLocale)
                }
                context.createConfigurationContext(config).resources
            }

            // Lắng nghe di chuyển màn hình, nhận event từ các viewmodel
            LaunchedEffect(Unit) {
                launch {
                    navController.currentBackStackEntryFlow.collect { entry ->
                        if (!isAdmobInitialized()) {
                            val currentRoute = entry.destination.route
                            if (currentRoute != NavigationScreen.SplashScreen.route) {
                                initAdsAndLoadAds()
                            }
                        }
                    }
                }
                routeNavigation
                    .navTransaction
                    .flowWithLifecycle(
                        lifecycle,
                        Lifecycle.State.STARTED,
                    )
                    .takeWhile {
                        routeNavigation.getScreenRoute() == ScreenRoute.HOME
                    }
                    .collectLatest {
                        when (it) {
                            is NavTransaction.NavigateToRoute -> {
                                navController.safeNavigate(route = it.route) {
                                    launchSingleTop = true
                                }
                            }

                            NavTransaction.Pop -> {
                                navController.popBackStack()
                            }

                            is NavTransaction.PopToRoute -> {
                                navController.popBackStack(
                                    route = it.route,
                                    inclusive = it.inclusive,
                                )
                            }

                            is NavTransaction.SetPreviousBackstackArgument<*> -> {
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    it.key,
                                    it.value,
                                )
                            }

                            is NavTransaction.SetBackstackArgument<*> -> {
                                val route = it.route
                                val key = it.key
                                val value = it.value
                                try {
                                    val entry = navController.getBackStackEntry(route)
                                    entry.savedStateHandle[key] = value
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            is NavTransaction.FinishApp -> {
                                finish()
                            }
                        }
                    }
            }

            CompositionLocalProvider(
                LocalResources provides resources,
                LocalIsPro provides mainUiState.isPro
//                LocalIsPro provides true
            ) {
                AppTheme(themeType = mainUiState.theme) {
                    Scaffold(
                        containerColor = AppTheme.color.colorBackgroundWindow
                    ) { innerPadding ->
                        Navigation(innerPadding, navController, mainUiState.onPurchased)
                    }
                }
            }
        }
    }

    override fun onUpdatePurchased(
        isPro: Boolean,
        needShowMessage: Boolean,
        needReload: Boolean
    ) {
        runOnUiThread {
            if (needShowMessage) {
                var messageShow = getString(R.string.purchase_success)
                if (!isPro) {
                    messageShow = getString(R.string.pro_restore_failed)
                }
                Toast.makeText(
                    this@MainActivity,
                    messageShow,
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (needReload && isPro) {
                viewModel.updateWhenPurchased(true)
            }
        }
        enableShowOpenForeground(!isPro, App.instance)
        if (!isPro) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != NavigationScreen.PremiumScreen.route) {
                initAdsAndLoadAds()
                viewModel.updateWhenPurchased(false)
            }
        } else {
            ControlAds.enableAutoRefresh = false
        }
    }

    override fun onUpdatePurchasedItem(
        productId: String,
        success: Boolean,
        purchaseToken: String?
    ) {
    }
}
