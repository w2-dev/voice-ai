package com.tapbi.spark.voiceai.ui.main.onboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.voiceai.common.models.Onboard
import com.tapbi.spark.voiceai.providers.LocalIsPro
import com.tapbi.spark.voiceai.ui.main.MainActivity
import com.tapbi.spark.ui_common_sdk.extension.OnLifecycleOwnerChanged
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_25
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1
import com.tapbi.spark.ui_common_sdk.foundation.grid_x12_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x3_75
import kotlinx.coroutines.launch
import mx.platacard.pagerindicator.PagerIndicator
import mx.platacard.pagerindicator.PagerIndicatorOrientation

@SuppressLint("ContextCastToActivity")
@Composable
fun OnboardScreen(viewModel: OnboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val latestActivity = LocalContext.current as? MainActivity
    val listOnBoard = uiState.data
    val isPro = LocalIsPro.current
    UpdateUi(listOnBoard) {
        viewModel.showAdsInter(isPro, latestActivity)
    }
    OnLifecycleOwnerChanged(
        onResume = {
            viewModel.nextScreen()
        },
    )
}

@Composable
private fun UpdateUi(
    listOnBoard: List<Onboard>,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { listOnBoard.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { index -> listOnBoard[index].image }
        ) { page ->
            OnboardItemContent(
                image = listOnBoard[page].image,
                textRes = listOnBoard[page].str
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = grid_x3_75, vertical = grid_x2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerIndicator(
                pagerState = pagerState,
                activeDotColor = AppTheme.color.colorFFD755,
                dotColor = Color.LightGray,
                dotCount = listOnBoard.size,
                orientation = PagerIndicatorOrientation.Horizontal
            )

            val isLastPage = pagerState.currentPage == listOnBoard.size - 1

            Text(
                modifier = Modifier
                    .border(
                        width = grid_x0_25,
                        color = AppTheme.color.colorFFD755,
                        shape = RoundedCornerShape(grid_x2_5)
                    )
                    .clickable {
                        if (!isLastPage) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    }
                    .padding(horizontal = grid_x1_5, vertical = grid_x1),
                text = stringResource(R.string.next),
                color = AppTheme.color.colorFFD755,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(grid_x3_75))
    }
}

@Composable
fun OnboardItemContent(image: Int, textRes: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(image),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            text = stringResource(textRes),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = grid_x3_75, vertical = grid_x12_5),
            color = AppTheme.color.colorWhite,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {

}