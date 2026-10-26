package com.tapbi.spark.voiceai.extension

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.safeNavigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    val currentRoute = this.currentDestination?.route
    if (currentRoute != route) {
        try {
            this.navigate(route, builder)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}