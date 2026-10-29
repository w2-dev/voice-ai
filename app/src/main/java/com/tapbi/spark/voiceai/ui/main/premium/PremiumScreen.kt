package com.tapbi.spark.voiceai.ui.main.premium

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ironman.spark.billing.BillingManager.mLiveDataBillingModels
import com.tapbi.spark.ui_common_sdk.components.ContentState
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonApp
import com.tapbi.spark.ui_common_sdk.extension.rippleClickable
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_125
import com.tapbi.spark.ui_common_sdk.foundation.grid_x0_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1_25
import com.tapbi.spark.ui_common_sdk.foundation.grid_x1_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2
import com.tapbi.spark.ui_common_sdk.foundation.grid_x2_5
import com.tapbi.spark.ui_common_sdk.foundation.grid_x3
import com.tapbi.spark.ui_common_sdk.foundation.grid_x4
import com.tapbi.spark.ui_common_sdk.foundation.round16
import com.tapbi.spark.ui_common_sdk.foundation.round8
import com.tapbi.spark.voiceai.R

/**
 * Màn hình Premium / Pro với tone xanh nhạt (Soft Light Blue Theme).
 * Cấu trúc: ProHeader -> ProContent -> ProFooter.
 */
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel,
    onPurchased: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity

    BackHandler {
        viewModel.onCloseClick(context)
    }

    LaunchedEffect(onPurchased) {
        if (onPurchased) {
            viewModel.popBackStack()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val billingModels by mLiveDataBillingModels.observeAsState(emptyList())

    LaunchedEffect(billingModels) {
        viewModel.updatePrice(billingModels, activity)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F8FC))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // 1. Header
        ProHeader(
            onCloseClick = { viewModel.onCloseClick(context) },
            modifier = Modifier.fillMaxWidth(),
        )

        // 2. Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (uiState.contentState) {
                ContentState.ERROR -> {
                    StateError {
                        viewModel.onClickRetry(activity)
                    }
                }

                else -> {
                    ProContent(
                        priceMonth = uiState.priceMonthly,
                        priceYear = uiState.priceYearly,
                        priceLifeTime = uiState.priceLifeTime,
                        selectedPlan = uiState.selectedPlan,
                        onPurchaseMonth = { viewModel.selectPlan(PlanType.MONTHLY) },
                        onPurchaseYear = { viewModel.selectPlan(PlanType.YEARLY) },
                        onPurchaseLifeTime = { viewModel.selectPlan(PlanType.LIFETIME) },
                        onSubscribeClick = { viewModel.subscribeSelectedPlan(activity) },
                        onPrivacyPolicyClick = { viewModel.openPolicy() },
                        onRestorePurchaseClick = {
                            activity?.let {
                                viewModel.queryCheckPro(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * Header chứa nút Close 'X' ở góc trên bên phải.
 */
@Composable
private fun ProHeader(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = grid_x2, vertical = grid_x1_5),
    ) {
        Box(
            modifier = Modifier
                .size(grid_x4)
                .clip(CircleShape)
                .background(Color(0xFFE3EFFB))
                .rippleClickable { onCloseClick() }
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0288D1)
            )
        }
    }
}

/**
 * Nội dung cuộn chính bao gồm Icon, Logo Voice AI PRO, Tagline và Card tính năng nổi bật.
 */
@Composable
private fun ProContent(
    priceMonth: String,
    priceYear: String,
    priceLifeTime: String,
    selectedPlan: PlanType,
    onPurchaseMonth: () -> Unit,
    onPurchaseYear: () -> Unit,
    onPurchaseLifeTime: () -> Unit,
    onSubscribeClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRestorePurchaseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = grid_x2, vertical = grid_x1),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE1F5FE)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(grid_x1_5))

        // App Name "Voice AI" + "PRO" Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Voice AI",
                style = AppTheme.typography.bold.copy(
                    fontSize = 28.sp,
                    lineHeight = TextUnit.Unspecified,
                    platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.width(grid_x1))

            // PRO Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(grid_x1))
                    .background(Color(0xFF29B6F6))
                    .padding(horizontal = grid_x1_25, vertical = grid_x0_5)
            ) {
                Text(
                    text = "PRO",
                    style = AppTheme.typography.bold.copy(
                        fontSize = 12.sp,
                        lineHeight = TextUnit.Unspecified,
                        platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(grid_x1))

        // Subtitle: Experience Voice AI Lock Screen with Full Premium Features ✨
        Text(
            text = "Experience Voice AI Lock Screen with Full Premium Features ✨",
            style = AppTheme.typography.medium.copy(
                fontSize = 14.sp,
                lineHeight = TextUnit.Unspecified,
                platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            color = Color(0xFF616161),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(grid_x2_5))

        // Card danh sách tính năng nổi bật Pro
        ProFeatureCard(
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(grid_x2))

        // Danh sách gói cước Pro
        ProPlanList(
            priceMonth = priceMonth,
            priceYear = priceYear,
            priceLifeTime = priceLifeTime,
            selectedPlan = selectedPlan,
            onPurchaseMonth = onPurchaseMonth,
            onPurchaseYear = onPurchaseYear,
            onPurchaseLifeTime = onPurchaseLifeTime,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(grid_x2))

        ProFooter(
            onSubscribeClick = { onSubscribeClick() },
            onPrivacyPolicyClick = { onPrivacyPolicyClick() },
            onRestorePurchaseClick = {
                onRestorePurchaseClick()
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Card danh sách 3 tính năng nổi bật của Voice AI Pro.
 */
@Composable
private fun ProFeatureCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = round16,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(grid_x0_125, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = grid_x0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = grid_x2_5, vertical = grid_x2_5),
            verticalArrangement = Arrangement.spacedBy(grid_x2)
        ) {
            ProFeatureRow(title = "Unlock with AI Voice & Speech Recognition")
            ProFeatureRow(title = "Unlimited Lock Screen Themes & Custom Styles")
            ProFeatureRow(title = "Ad-Free & Advanced Security Protection")
        }
    }
}

@Composable
fun StateError(
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(grid_x2),
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

@Composable
private fun ProFeatureRow(
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(grid_x1_5)
    ) {
        Icon(
            painter = painterResource(id = AppTheme.drawable.icDone),
            contentDescription = null,
            tint = Color(0xFF29B6F6),
            modifier = Modifier.size(grid_x2_5)
        )

        Text(
            text = title,
            style = AppTheme.typography.medium.copy(
                fontSize = 14.sp,
                lineHeight = TextUnit.Unspecified,
                platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            color = Color(0xFF212121)
        )
    }
}

/**
 * Giao diện hiển thị các gói cước theo yêu cầu.
 */
@Composable
private fun ProPlanList(
    priceMonth: String,
    priceYear: String,
    priceLifeTime: String,
    selectedPlan: PlanType,
    onPurchaseMonth: () -> Unit,
    onPurchaseYear: () -> Unit,
    onPurchaseLifeTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(grid_x1_5)
    ) {
        // Gói Monthly
        PlanItemCard(
            title = "Monthly Plan",
            price = priceMonth,
            isSelected = selectedPlan == PlanType.MONTHLY,
            onClick = onPurchaseMonth
        )

        // Gói Yearly (Nổi bật - BEST OFFER)
        PlanItemCard(
            title = "Yearly Plan",
            price = priceYear,
            isSelected = selectedPlan == PlanType.YEARLY,
            onClick = onPurchaseYear,
            isYearly = true
        )

        // Gói Lifetime
        PlanItemCard(
            title = "Lifetime Plan",
            price = priceLifeTime,
            isSelected = selectedPlan == PlanType.LIFETIME,
            onClick = onPurchaseLifeTime
        )
    }
}

@Composable
private fun PlanItemCard(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isYearly: Boolean = false,
    subtitle: String? = null,
    badgeText: String? = null,
) {
    val displayBadge =
        badgeText ?: if (isYearly) "BEST OFFER" else null
    val hasBadge = !displayBadge.isNullOrEmpty()

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasBadge) Modifier.padding(top = grid_x1) else Modifier)
                .clip(round16)
                .rippleClickable { onClick() },
            shape = round16,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(
                if (isSelected) 2.dp else grid_x0_125,
                if (isSelected) Color(0xFF29B6F6) else Color(0xFFE0E0E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = grid_x0)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = grid_x2_5, vertical = grid_x2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.bold.copy(fontSize = 16.sp),
                    color = Color(0xFF212121)
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = price,
                        textAlign = TextAlign.End,
                        style = AppTheme.typography.bold.copy(fontSize = 16.sp),
                        color = Color(0xFF212121)
                    )

                    val displaySubtitle = subtitle
                        ?: if (isYearly) "Save 2 months" else null
                    if (!displaySubtitle.isNullOrEmpty()) {
                        Text(
                            text = displaySubtitle,
                            style = AppTheme.typography.normal.copy(fontSize = 12.sp),
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .padding(start = grid_x2_5)
                    .clip(RoundedCornerShape(grid_x1))
                    .background(Color(0xFF29B6F6))
                    .padding(horizontal = grid_x1_25, vertical = grid_x0_5)
            ) {
                Text(
                    text = displayBadge,
                    style = AppTheme.typography.bold.copy(fontSize = 11.sp),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Component ProFooter chứa nút Subscribe Now và các liên kết Privacy Policy & Restore Purchase.
 * Luôn được cố định ở đáy màn hình (Bottom).
 */
@Composable
private fun ProFooter(
    onSubscribeClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRestorePurchaseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F8FC))
            .padding(horizontal = grid_x2, vertical = grid_x2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonApp(
            text = "Subscribe Now",
            onClick = onSubscribeClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(grid_x2))

        Row(
            horizontalArrangement = Arrangement.spacedBy(grid_x3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.privacy_policy),
                style = AppTheme.typography.normal.copy(
                    fontSize = 13.sp,
                    lineHeight = TextUnit.Unspecified,
                    textDecoration = TextDecoration.Underline,
                    platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = Color(0xFF757575),
                modifier = Modifier
                    .clip(round8)
                    .rippleClickable { onPrivacyPolicyClick() }
                    .padding(horizontal = grid_x1_5, vertical = grid_x0_5)
            )

            Text(
                text = "Restore Purchase",
                style = AppTheme.typography.normal.copy(
                    fontSize = 13.sp,
                    lineHeight = TextUnit.Unspecified,
                    textDecoration = TextDecoration.Underline,
                    platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = Color(0xFF757575),
                modifier = Modifier
                    .clip(round8)
                    .rippleClickable { onRestorePurchaseClick() }
                    .padding(horizontal = grid_x1_5, vertical = grid_x0_5)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FC)
@Composable
private fun PremiumScreenPreview() {
    PremiumScreen(
        viewModel = hiltViewModel(),
        onPurchased = false
    )
}
