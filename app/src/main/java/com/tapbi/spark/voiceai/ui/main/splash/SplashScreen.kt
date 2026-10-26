package com.tapbi.spark.voiceai.ui.main.splash

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x5
import com.tapbi.spark.voiceai.providers.LocalIsPro

@SuppressLint("ContextCastToActivity")
@Composable
fun SplashScreen(splashViewModel: SplashViewModel) {
    val isPro = LocalIsPro.current

    LaunchedEffect(Unit) {
        splashViewModel.startSplashTimer()
    }

    UpdateUI(!isPro)
}

@Composable
fun UpdateUI(showLoading: Boolean) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(AppTheme.assetsData.splash)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
//        LottieAnimation(
//            composition = composition,
//            modifier = Modifier.align(Alignment.Center)
//        )
        ShowLoading(
            showLoading,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = grid_x5)
        )
    }
}

@Composable
fun ShowLoading(showLoading: Boolean, modifier: Modifier = Modifier) {
    if (showLoading) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(grid_x2_5)
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    UpdateUI(true)
}
