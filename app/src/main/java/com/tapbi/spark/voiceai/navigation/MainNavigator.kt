package com.tapbi.spark.voiceai.navigation

import com.tapbi.spark.voiceai.navigation.utils.RouteNavigation
import javax.inject.Inject

interface MainNavigator {

    fun openOnboardingFromLanguage()

    fun openLanguageFromSplash()

    fun openHomeFromSplash()

    fun openHomeFromOnboard()

    fun openVoiceSetup()

    fun openPassphraseSetup()

    fun openPatternSetup()

    fun popBackStack()

    fun navigateTo(route: String)

    fun finishApp()
}

internal class MainNavigatorImpl @Inject constructor(
    private val routeNavigation: RouteNavigation,
) : MainNavigator {

    override fun openOnboardingFromLanguage() {
        routeNavigation.navigate(
            NavigationScreen.OnboardScreen.route,
            popToRoute = NavigationScreen.LanguageScreen.route,
            inclusive = true,
        )
    }

    override fun openLanguageFromSplash() {
        routeNavigation.navigate(
            NavigationScreen.LanguageScreen.route,
            popToRoute = NavigationScreen.SplashScreen.route,
            inclusive = true,
        )
    }

    override fun openHomeFromSplash() {
        routeNavigation.navigate(
            NavigationScreen.HomeScreen.route,
            popToRoute = NavigationScreen.SplashScreen.route,
            inclusive = true,
        )
    }

    override fun openHomeFromOnboard() {
        routeNavigation.navigate(
            NavigationScreen.HomeScreen.route,
            popToRoute = NavigationScreen.OnboardScreen.route,
            inclusive = true,
        )
    }

    override fun openVoiceSetup() {
        routeNavigation.navigate(NavigationScreen.VoiceSetupScreen.route)
    }

    override fun openPassphraseSetup() {
        routeNavigation.navigate(NavigationScreen.PassphraseSetupScreen.route)
    }

    override fun openPatternSetup() {
        routeNavigation.navigate(NavigationScreen.PatternSetupScreen.route)
    }

    override fun popBackStack() {
        routeNavigation.pop()
    }

    override fun navigateTo(route: String) {
        routeNavigation.navigate(route)
    }

    override fun finishApp() {
        routeNavigation.finishApp()
    }
}
