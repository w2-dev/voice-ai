package com.tapbi.spark.voiceai.navigation.utils

sealed interface NavTransaction {
    class NavigateToRoute(
        val route: String,
    ) : NavTransaction

    class PopToRoute(
        val route: String,
        val inclusive: Boolean = false,
    ) : NavTransaction

    class SetPreviousBackstackArgument<T>(
        val key: String,
        val value: T,
    ) : NavTransaction

    class SetBackstackArgument<T>(
        val route: String,
        val key: String,
        val value: T,
    ) : NavTransaction

    data object Pop : NavTransaction

    data object FinishApp : NavTransaction
}