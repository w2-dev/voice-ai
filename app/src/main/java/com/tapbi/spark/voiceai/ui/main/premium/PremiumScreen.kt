package com.tapbi.spark.voiceai.ui.main.premium

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ironman.spark.billing.BillingManager.mLiveDataBillingModels
import com.tapbi.spark.voiceai.R
import com.tapbi.spark.ui_common_sdk.components.ContentState
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2
import timber.log.Timber

@Composable
fun PremiumScreen(viewModel: PremiumViewModel, onPurchased: Boolean) {
    LaunchedEffect(onPurchased) {
        if (onPurchased) {
            viewModel.popBackStack()
        }
    }
    val context = LocalContext.current
    val activity = context as? AppCompatActivity
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val billingModels by mLiveDataBillingModels
        .observeAsState(emptyList())
    LaunchedEffect(billingModels) {
        viewModel.updatePrice(billingModels, activity)
    }

    when (uiState.contentState) {
        ContentState.ERROR -> {
            StateError {
                viewModel.onClickRetry(activity)
            }
        }

        else -> {
            UpdatePrice(
                uiState.priceMonthly,
                uiState.priceYearly,
                uiState.priceLifeTime,
                onPurchaseMonth = {
                    viewModel.onPurchaseMonth(activity)
                },
                onPurchaseYear = {
                    viewModel.onPurchaseYear(activity)
                },
                onPurchaseLifeTime = {
                    viewModel.onPurchaseLifeTime(activity)
                })
        }
    }
}

@Composable
fun UpdatePrice(
    priceMonth: String,
    priceYear: String,
    priceLifeTime: String,
    onPurchaseMonth: () -> Unit,
    onPurchaseYear: () -> Unit,
    onPurchaseLifeTime: () -> Unit
) {
    Timber.e("HaiPd UpdatePrice: ")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(modifier = Modifier.fillMaxWidth(fraction = 0.6f), onClick = {
            onPurchaseMonth()
        }) {
            Text(priceMonth)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(modifier = Modifier.fillMaxWidth(fraction = 0.6f), onClick = {
            onPurchaseYear()
        }) {
            Text(priceYear)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(fraction = 0.6f),
            onClick = { onPurchaseLifeTime() }) {
            Text(priceLifeTime)
        }
    }
}

@Composable
fun StateError(
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.billing_error_connection)
        )

        Spacer(modifier = Modifier.height(grid_x2))

        Button(
            onClick = onTryAgain
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
}