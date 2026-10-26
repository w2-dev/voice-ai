package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageHeader(
    modifier: Modifier = Modifier,
    title: String,
    iconBack: Int,
    iconFinish: Int,
    isHideBack: Boolean = false,
    isHideAction: Boolean = false,
    actionBack: () -> Unit,
    actionFinish: () -> Unit,
) {
    val density = LocalDensity.current
    var safePadding by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
    ) {
        Text(
            text = title,
            color = AppTheme.color.colorWhite,
            style = AppTheme.typography.medium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = with(density) { safePadding.toDp() })
        )

        if (!isHideBack) {
            DebounceIconButton(
                onClick = actionBack,
                modifier = Modifier
                    .padding(start = grid_x1)
                    .onSizeChanged { size ->
                        safePadding = max(safePadding, size.width)
                    }
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    modifier = Modifier,
                    painter = painterResource(id = iconBack),
                    contentDescription = "Back",
                    tint = AppTheme.color.colorBlack
                )
            }
        }

        DebounceIconButton(
            onClick = {
                if (!isHideAction)
                    actionFinish()
            },
            modifier = Modifier
                .alpha(if (isHideAction) 0f else 1f)
                .padding(end = grid_x1)
                .onSizeChanged { size ->
                    safePadding = max(safePadding, size.width)
                }
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                modifier = Modifier,
                painter = painterResource(id = iconFinish),
                contentDescription = "Finish",
                tint = AppTheme.color.colorFFD755
            )
        }
    }

}
