package com.tapbi.spark.ui_common_sdk.components.loadings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.tapbi.spark.ui_common_sdk.R
import com.tapbi.spark.ui_common_sdk.components.ui.BodyMediumText
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_75
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2


@Composable
fun CircularLoadingScreen(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.color.colorFFD755
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color
        )
    }
}

@Composable
fun ViewLoadingScreen(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.color.colorFFD755
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(grid_x2))
            .background(AppTheme.color.colorWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = color
            )
            Spacer(Modifier.height(grid_x0_75))
            BodyMediumText(
                text = stringResource(R.string.loading),
                color = AppTheme.color.colorFFD755,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun PreviewUI() {
}