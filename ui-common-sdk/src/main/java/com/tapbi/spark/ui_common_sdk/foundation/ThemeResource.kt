package com.tapbi.spark.ui_common_sdk.foundation

enum class ThemeType {
    USER_DEFAULT,
    USER_APPROVE,
}

data class ThemeResource(
    val colors: ColorApp,
    val drawable: DrawableApp,
    val brush: AppBrush,
    val assetsData: AssetsDataApp
)