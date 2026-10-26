package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

object AssetsDefinition {
    const val SPLASH_JSON = "splash.json"
}

@Immutable
data class AssetsDataApp(
    val splash: String = AssetsDefinition.SPLASH_JSON
)

internal val LocalAppAssetsData = staticCompositionLocalOf { assetsDataDefault }

val assetsDataDefault = AssetsDataApp(
    splash = AssetsDefinition.SPLASH_JSON
)

val assetsDataApprove = AssetsDataApp(
    splash = AssetsDefinition.SPLASH_JSON
)
