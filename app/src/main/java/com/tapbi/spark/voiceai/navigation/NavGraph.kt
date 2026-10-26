package com.tapbi.spark.voiceai.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.voiceai.navigation.utils.NavAnimType
import com.tapbi.spark.voiceai.navigation.utils.animatedComposable
import com.tapbi.spark.voiceai.ui.main.home.HomeScreen
import com.tapbi.spark.voiceai.ui.main.home.HomeViewModel
import com.tapbi.spark.voiceai.ui.main.language.LanguageScreen
import com.tapbi.spark.voiceai.ui.main.language.LanguageViewModel
import com.tapbi.spark.voiceai.ui.main.onboard.OnboardScreen
import com.tapbi.spark.voiceai.ui.main.onboard.OnboardViewModel
import com.tapbi.spark.voiceai.ui.main.pattern.PatternSetupScreen
import com.tapbi.spark.voiceai.ui.main.pattern.PatternSetupViewModel
import com.tapbi.spark.voiceai.ui.main.pin.PinSetupScreen
import com.tapbi.spark.voiceai.ui.main.pin.PinSetupViewModel
import com.tapbi.spark.voiceai.ui.main.premium.PremiumScreen
import com.tapbi.spark.voiceai.ui.main.premium.PremiumViewModel
import com.tapbi.spark.voiceai.ui.main.splash.SplashScreen
import com.tapbi.spark.voiceai.ui.main.splash.SplashViewModel
import com.tapbi.spark.voiceai.ui.main.voice.PassphraseSetupScreen
import com.tapbi.spark.voiceai.ui.main.voice.VoiceSetupScreen
import com.tapbi.spark.voiceai.ui.main.voice.VoiceSetupViewModel

@Composable
fun Navigation(
    innerPadding: PaddingValues,
    navController: NavHostController,
    onPurchased: Boolean
) {
    NavHost(navController, startDestination = NavigationScreen.SplashScreen.route) {
        composable(NavigationScreen.SplashScreen.route) {
            val viewModel = hiltViewModel<SplashViewModel>()
            SplashScreen(viewModel)
        }
        animatedComposable(
            NavigationScreen.OnboardScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<OnboardViewModel>()
            OnboardScreen(viewModel)
        }
        animatedComposable(
            NavigationScreen.LanguageScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<LanguageViewModel>()

            LanguageScreen(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(color = AppTheme.color.colorBackgroundWindow)
                    .fillMaxSize(), viewModel
            )
        }
        animatedComposable(
            NavigationScreen.HomeScreen.route,
            animType = NavAnimType.Elevate
        ) {
            val viewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(
                viewModel, modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            )
        }
        animatedComposable(
            NavigationScreen.PremiumScreen.route,
            animType = NavAnimType.Elevate
        ) {
            val viewModel = hiltViewModel<PremiumViewModel>()
            PremiumScreen(viewModel, onPurchased)
        }
        animatedComposable(
            NavigationScreen.VoiceSetupScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<VoiceSetupViewModel>()
            VoiceSetupScreen(
                viewModel, modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            )
        }
        animatedComposable(
            NavigationScreen.PassphraseSetupScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<VoiceSetupViewModel>()
            PassphraseSetupScreen(
                viewModel, modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            )
        }
        animatedComposable(
            NavigationScreen.PatternSetupScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<PatternSetupViewModel>()
            PatternSetupScreen(
                viewModel, modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            )
        }
        animatedComposable(
            NavigationScreen.PinSetupScreen.route,
            animType = NavAnimType.SlideAndFade
        ) {
            val viewModel = hiltViewModel<PinSetupViewModel>()
            PinSetupScreen(
                viewModel, modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            )
        }
    }
}
