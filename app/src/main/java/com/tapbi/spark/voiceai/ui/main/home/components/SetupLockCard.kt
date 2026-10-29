package com.tapbi.spark.voiceai.ui.main.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Section 4: Thẻ danh sách các mục cài đặt nhanh Setup Lock
 */
@Composable
fun SetupLockCard(
    onVoiceSetup: () -> Unit,
    onPinSetup: () -> Unit,
    onPatternSetup: () -> Unit,
    onFingerprintSetup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Setup Lock",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            // 2x2 Setup Item Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFFF0F5),
                        icon = { WaveformIcon(modifier = Modifier.size(20.dp)) },
                        title = "Voice setup",
                        onClick = onVoiceSetup
                    )

                    // Pin setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFFF0F5),
                        icon = { PinIcon(modifier = Modifier.size(20.dp)) },
                        title = "Pin setup",
                        onClick = onPinSetup
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pattern setup
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFE1F5FE),
                        icon = { PatternIcon(modifier = Modifier.size(20.dp)) },
                        title = "Pattern setup",
                        onClick = onPatternSetup
                    )

                    // Fingerprint
                    SetupItemBox(
                        modifier = Modifier.weight(1f),
                        iconBgColor = Color(0xFFFCE4EC),
                        icon = { FingerprintIcon(modifier = Modifier.size(20.dp)) },
                        title = "Fingerprint",
                        onClick = onFingerprintSetup
                    )
                }
            }
        }
    }
}

/**
 * Mỗi ô thiết lập loại khóa trong Setup Lock
 */
@Composable
private fun SetupItemBox(
    modifier: Modifier = Modifier,
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8F9FB))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )

                Text(
                    text = "›",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
