package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tapbi.spark.ui_common_sdk.extension.rippleClickable
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHeader(
    title: String,
    iconBack: Int,
    iconFinish: Int,
    actionBack: () -> Unit,
    actionFinish: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            NormalText(
                text = title,
                color = Color.White,
                style = AppTheme.typography.semiBold
            )
        },
        navigationIcon = {
            HeaderIcon(
                icon = iconBack,
                contentDescription = "Back",
                onClick = actionBack
            )
        },
        actions = {
            HeaderIcon(
                icon = iconFinish,
                contentDescription = "Finish",
                onClick = actionFinish
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.color.colorBlack),
        windowInsets = WindowInsets(0)
    )
}

@Composable
private fun HeaderIcon(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Image(
        modifier = Modifier
            .padding(grid_x1)
            .rippleClickable(
                rippleColor = AppTheme.color.colorWhite
            ) {
                onClick()
            }
            .padding(grid_x1),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit
    )
}