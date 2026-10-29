package com.tapbi.spark.voiceai.ui.main.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dòng hiển thị loại khóa kèm nút bật/tắt (Switch)
 */
@Composable
fun LockMethodToggleRow(
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }

            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF29B6F6),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.size(width = 40.dp, height = 24.dp)
            )
        }
    }
}

// Vector Icons
@Composable
fun WaveformIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        val barWidth = 2.dp.toPx()
        val heights = listOf(0.4f, 0.8f, 1.0f, 0.6f, 0.9f, 0.5f)
        val startX = c.x - (heights.size * barWidth * 1.5f) / 2f

        heights.forEachIndexed { i, factor ->
            val x = startX + i * barWidth * 2f
            val barH = h * 0.6f * factor
            drawLine(
                color = Color(0xFF29B6F6),
                start = Offset(x, c.y - barH / 2f),
                end = Offset(x, c.y + barH / 2f),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
fun PinIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dotR = 2.dp.toPx()
        val startX = w * 0.25f
        val gap = w * 0.25f
        val cy = h * 0.5f

        for (i in 0..2) {
            drawCircle(
                color = Color(0xFF29B6F6),
                radius = dotR,
                center = Offset(startX + i * gap, cy)
            )
        }
    }
}

@Composable
fun PatternIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dotR = 1.8.dp.toPx()
        val offsets = listOf(
            Offset(w * 0.3f, h * 0.3f),
            Offset(w * 0.7f, h * 0.3f),
            Offset(w * 0.3f, h * 0.7f),
            Offset(w * 0.7f, h * 0.7f)
        )

        val path = Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            lineTo(offsets[1].x, offsets[1].y)
            lineTo(offsets[3].x, offsets[3].y)
            lineTo(offsets[2].x, offsets[2].y)
        }
        drawPath(path, color = Color(0xFF29B6F6), style = Stroke(width = 1.5.dp.toPx()))

        offsets.forEach {
            drawCircle(color = Color(0xFF29B6F6), radius = dotR, center = it)
        }
    }
}

@Composable
fun FingerprintIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 1..3) {
            val arcSize = Size(w * 0.22f * i, h * 0.22f * i)
            drawArc(
                color = Color(0xFFEC407A),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(
                    w / 2f - arcSize.width / 2f,
                    h / 2f - arcSize.height / 2f + i * 1.5.dp.toPx()
                ),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
fun PowerIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawArc(
            color = tint,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.7f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        drawLine(
            color = tint,
            start = Offset(w / 2f, h * 0.1f),
            end = Offset(w / 2f, h * 0.45f),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}
