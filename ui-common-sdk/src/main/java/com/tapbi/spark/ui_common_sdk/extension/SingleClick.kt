package com.tapbi.spark.ui_common_sdk.extension

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

private var lastClick = 0L
const val TIME_DISTANCE_SHORT_CLICK = 300
const val TIME_DISTANCE_LONG_CLICK = 600
const val ALPHA_TOUCH_CLICK = 0.5f

fun singleClick(
    timeDistance: Int = TIME_DISTANCE_SHORT_CLICK,
    onSingleClick: () -> Unit,
) {
    if (abs(System.currentTimeMillis() - lastClick) < timeDistance) {
        return
    }
    lastClick = System.currentTimeMillis()
    onSingleClick()
}

fun singleLongClick(singleLongClick: () -> Unit) {
    if (abs(System.currentTimeMillis() - lastClick) < TIME_DISTANCE_LONG_CLICK) {
        return
    }
    lastClick = System.currentTimeMillis()
    singleLongClick()
}


@SuppressLint("ModifierFactoryUnreferencedReceiver")
inline fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    crossinline onClick: () -> Unit,
): Modifier =
    composed {
        clickable(
            enabled = enabled,
            indication = null,
            interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        ) {
            onClick()
        }
    }

@SuppressLint("ModifierFactoryUnreferencedReceiver")
inline fun Modifier.rippleClickable(
    enabled: Boolean = true,
    bounded: Boolean = false,
    timeDistance: Int = TIME_DISTANCE_SHORT_CLICK,
    interactionSource: MutableInteractionSource? = null,
    rippleColor: Color = Color.Unspecified,
    crossinline onClick: () -> Unit,
): Modifier =
    composed {
        clickable(
            enabled = enabled,
            indication = ripple(bounded = bounded, color = rippleColor),
            interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        ) {
            singleClick(timeDistance) {
                onClick()
            }
        }
    }