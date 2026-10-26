package com.tapbi.spark.ui_common_sdk.foundation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tapbi.spark.ui_common_sdk.R

@Immutable
data class DrawableApp(
    @param:DrawableRes val icActiveRadio: Int = R.drawable.ic_actived_radio_button,
    @param:DrawableRes val icBack: Int = R.drawable.ic_back,
    @param:DrawableRes val icDone: Int = R.drawable.ic_done
)

internal val LocalAppDrawable = staticCompositionLocalOf { drawableDefault }

val drawableDefault = DrawableApp()

val drawableApprove =
    DrawableApp(
        icActiveRadio = R.drawable.ic_actived_radio_button,
        icBack = R.drawable.ic_back,
        icDone = R.drawable.ic_done,
    )