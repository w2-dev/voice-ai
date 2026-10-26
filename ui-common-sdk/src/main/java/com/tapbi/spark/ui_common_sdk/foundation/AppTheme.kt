package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme.brush

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTheme(
    themeType: ThemeType = ThemeType.USER_DEFAULT,
    content: @Composable () -> Unit
) {
    val themeResource =
        if (themeType == ThemeType.USER_DEFAULT) {
            ThemeResource(colorDefault, drawableDefault, brushDefault, assetsDataDefault)
        } else {
            ThemeResource(colorApprove, drawableApprove, brushApprove, assetsDataApprove)
        }
    CompositionLocalProvider(
        LocalOverscrollFactory provides null,
        LocalAppColor provides themeResource.colors,
        LocalAppDrawable provides themeResource.drawable,
        LocalAppBrush provides themeResource.brush,
        LocalAppAssetsData provides themeResource.assetsData,
    ) {
        MaterialTheme(
            typography = TypographyApp().materialTypography,
            shapes = ShapeApp.materialShapes,
            content = content,
        )
    }
}

object AppTheme {
    val color: ColorApp
        @Composable
        get() = LocalAppColor.current

    val drawable: DrawableApp
        @Composable
        get() = LocalAppDrawable.current

    val brush: AppBrush
        @Composable
        get() = LocalAppBrush.current

    val assetsData: AssetsDataApp
        @Composable
        get() = LocalAppAssetsData.current

    val typography: TypographyApp
        @Composable
        get() = TypographyApp()
}