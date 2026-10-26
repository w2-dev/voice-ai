package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme

@Composable
fun RadioApp(modifier: Modifier = Modifier, isSelected: Boolean) {
    val tintIcon = if (isSelected) AppTheme.color.colorFFD755 else AppTheme.color.color5A5D5D
    val colorFilter = remember(tintIcon) { ColorFilter.tint(tintIcon) }
    Image(
        modifier = modifier,
        painter = painterResource(id = AppTheme.drawable.icActiveRadio),
        contentDescription = "",
        colorFilter = colorFilter
    )

}