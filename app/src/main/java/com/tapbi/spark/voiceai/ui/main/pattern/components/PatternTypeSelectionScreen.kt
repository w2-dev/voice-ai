package com.tapbi.spark.voiceai.ui.main.pattern.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonApp
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonAppStyle
import com.tapbi.spark.voiceai.R
import androidx.compose.runtime.LaunchedEffect
import com.tapbi.spark.voiceai.ui.main.pattern.PatternGridType
import com.tapbi.spark.voiceai.ui.main.pattern.PatternSetupStep
import com.tapbi.spark.voiceai.ui.main.pattern.PatternUiState

@Composable
fun PatternTypeSelectionScreen(
    uiState: PatternUiState,
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onSelectType: (rows: Int, cols: Int) -> Unit,
    onApplyClick: () -> Unit,
    onResetSetup: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onResetSetup()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Pattern Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPremiumClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stepper
        PatternStepperHeader(currentStep = PatternSetupStep.CHOOSE_TYPE)

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        val stepTitle = buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)) {
                append("Step 1: ")
            }
            withStyle(SpanStyle(color = Color(0xFF212121), fontWeight = FontWeight.Bold)) {
                append("Choose your type")
            }
        }

        Text(
            text = stepTitle,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2x2 Grid Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val types = PatternUiState.AVAILABLE_TYPES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GridTypeCard(
                        type = types[0],
                        isSelected = uiState.selectedRows == types[0].rows && uiState.selectedCols == types[0].cols,
                        onClick = { onSelectType(types[0].rows, types[0].cols) },
                        modifier = Modifier.weight(1f)
                    )
                    GridTypeCard(
                        type = types[1],
                        isSelected = uiState.selectedRows == types[1].rows && uiState.selectedCols == types[1].cols,
                        onClick = { onSelectType(types[1].rows, types[1].cols) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GridTypeCard(
                        type = types[2],
                        isSelected = uiState.selectedRows == types[2].rows && uiState.selectedCols == types[2].cols,
                        onClick = { onSelectType(types[2].rows, types[2].cols) },
                        modifier = Modifier.weight(1f)
                    )
                    GridTypeCard(
                        type = types[3],
                        isSelected = uiState.selectedRows == types[3].rows && uiState.selectedCols == types[3].cols,
                        onClick = { onSelectType(types[3].rows, types[3].cols) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ButtonApp(
            text = "APPLY NOW",
            onClick = onApplyClick,
            style = ButtonAppStyle.OK,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp)
        )
    }
}

@Composable
fun PatternStepperHeader(currentStep: PatternSetupStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val step1Done = currentStep != PatternSetupStep.CHOOSE_TYPE
        val step2Done =
            currentStep == PatternSetupStep.CONFIRM_DRAW || currentStep == PatternSetupStep.SUCCESS
        val step3Done = currentStep == PatternSetupStep.SUCCESS

        // Circle 1
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFF0288D1), CircleShape)
                .background(if (step1Done) Color(0xFF0288D1) else Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (step1Done) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // Line 1-2
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(if (step1Done) Color(0xFF0288D1) else Color(0xFFE0E0E0))
        )

        // Circle 2
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    if (step1Done) Color(0xFF0288D1) else Color(0xFFE0E0E0),
                    CircleShape
                )
                .background(if (step2Done) Color(0xFF0288D1) else Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (step2Done) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // Line 2-3
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(if (step2Done) Color(0xFF0288D1) else Color(0xFFE0E0E0))
        )

        // Circle 3
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    if (step2Done) Color(0xFF0288D1) else Color(0xFFE0E0E0),
                    CircleShape
                )
                .background(if (step3Done) Color(0xFF0288D1) else Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (step3Done) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun GridTypeCard(
    type: PatternGridType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF0288D1) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE1F5FE).copy(alpha = 0.5f) else Color(
                0xFFF5F7FA
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = type.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            PatternMiniPreview(
                rows = type.rows,
                cols = type.cols,
                samplePattern = type.samplePattern,
                isSelected = isSelected,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun PatternMiniPreview(
    rows: Int,
    cols: Int,
    samplePattern: List<Int>,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cellW = w / cols
        val cellH = h / rows
        val radius = minOf(cellW, cellH) / 6f

        val total = rows * cols
        val centers = (0 until total).map { idx ->
            val r = idx / cols
            val c = idx % cols
            Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
        }

        if (samplePattern.isNotEmpty()) {
            val path = Path().apply {
                moveTo(centers[samplePattern[0]].x, centers[samplePattern[0]].y)
                for (i in 1 until samplePattern.size) {
                    lineTo(centers[samplePattern[i]].x, centers[samplePattern[i]].y)
                }
            }
            drawPath(
                path = path,
                color = if (isSelected) Color(0xFF0288D1) else Color(0xFF0288D1).copy(alpha = 0.6f),
                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        for (i in 0 until total) {
            val center = centers[i]
            val isNodeSelected = samplePattern.contains(i)
            drawCircle(
                color = when {
                    isNodeSelected -> Color(0xFF0288D1)
                    isSelected -> Color(0xFF81D4FA)
                    else -> Color(0xFFCFD8DC)
                },
                radius = if (isNodeSelected) radius * 1.3f else radius,
                center = center
            )
        }
    }
}
