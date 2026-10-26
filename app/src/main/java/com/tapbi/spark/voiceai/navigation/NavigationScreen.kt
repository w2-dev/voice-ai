package com.tapbi.spark.voiceai.navigation

sealed class NavigationScreen(
    val route: String,
) {
    data object SplashScreen : NavigationScreen("SplashScreen")
    data object LanguageScreen : NavigationScreen("LanguageScreen")
    data object OnboardScreen : NavigationScreen("OnboardScreen")
    data object HomeScreen : NavigationScreen("HomeScreen")
    data object PremiumScreen : NavigationScreen("PremiumScreen")
    data object VoiceSetupScreen : NavigationScreen("VoiceSetupScreen")
    data object PassphraseSetupScreen : NavigationScreen("PassphraseSetupScreen")
    data object PatternSetupScreen : NavigationScreen("PatternSetupScreen")
    data object PinSetupScreen : NavigationScreen("PinSetupScreen")
    data object SecurityQuestionScreen : NavigationScreen("SecurityQuestionScreen")
}
