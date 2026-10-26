package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush

@Immutable
data class AppBrush(
    val primaryBrush: Brush,
    val secondaryBrush: Brush
)

internal val LocalAppBrush = staticCompositionLocalOf { brushDefault }

val brushDefault =
    AppBrush(
        primaryBrush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        ColorDefinition.ColorFFD755,
                        ColorDefinition.White,
                    )
            ),
        secondaryBrush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        ColorDefinition.ColorFFD755,
                        ColorDefinition.Black,
                    )
            )
    )

val brushApprove =
    AppBrush(
        primaryBrush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        ColorDefinition.ColorFFD755,
                        ColorDefinition.White,
                    )
            ),
        secondaryBrush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        ColorDefinition.ColorFFD755,
                        ColorDefinition.Black,
                    )
            )
    )
