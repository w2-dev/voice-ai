package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme

@Composable
fun NormalText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = AppTheme.typography.normal,
    color: Color = AppTheme.color.colorWhite,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style,
        overflow = overflow
    )
}

@Composable
fun BodyMediumText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = AppTheme.typography.medium,
    color: Color = AppTheme.color.colorWhite,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style,
        overflow = overflow,
        textAlign = textAlign
    )
}
