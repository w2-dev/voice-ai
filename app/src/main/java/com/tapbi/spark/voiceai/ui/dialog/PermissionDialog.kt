package com.tapbi.spark.voiceai.ui.dialog

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tapbi.spark.ui_common_sdk.components.ui.AnimateDialog
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonApp
import com.tapbi.spark.ui_common_sdk.components.ui.ButtonAppStyle
import com.tapbi.spark.voiceai.extension.hasNotificationPermission
import com.tapbi.spark.voiceai.extension.hasOverlayPermission
import com.tapbi.spark.voiceai.extension.hasRecordAudioPermission
import com.tapbi.spark.voiceai.extension.isAtLeastApi33
import com.tapbi.spark.voiceai.extension.isIgnoringBatteryOptimizations
import com.tapbi.spark.voiceai.extension.openAutostartSettings
import com.tapbi.spark.voiceai.extension.openBatteryOptimizationSettings
import com.tapbi.spark.voiceai.extension.openOverlayPermissionSettings

@Composable
fun PermissionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onAllPermissionsGranted: () -> Unit
) {
    if (!showDialog) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sharedPrefs = remember(context) {
        context.getSharedPreferences("app_permission_prefs", android.content.Context.MODE_PRIVATE)
    }
    var isAutostartClicked by remember { androidx.compose.runtime.mutableStateOf(false) }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isAutostartClicked) {
                    sharedPrefs.edit().putBoolean("autostart_configured", true).apply()
                }
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTrigger++
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTrigger++
    }

    val isOverlayGranted = remember(refreshTrigger) { context.hasOverlayPermission() }
    val isAudioGranted = remember(refreshTrigger) { context.hasRecordAudioPermission() }
    val isNotificationGranted = remember(refreshTrigger) { context.hasNotificationPermission() }
    val isBatteryGranted = remember(refreshTrigger) { context.isIgnoringBatteryOptimizations() }
    val isAutostartGranted = remember(refreshTrigger) {
        sharedPrefs.getBoolean("autostart_configured", false) || isAutostartClicked
    }

    val areMandatoryGranted = isOverlayGranted && isAudioGranted

    AnimateDialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cấp Quyền Cho Ứng Dụng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Vui lòng cấp các quyền dưới đây để tính năng Khóa Màn Hình hoạt động đầy đủ:",
                    fontSize = 13.sp,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionRowItem(
                        iconText = "🛡️",
                        title = "Hiển thị trên ứng dụng khác",
                        description = "Bắt buộc để hiển thị màn hình khóa Voice AI",
                        isGranted = isOverlayGranted,
                        onRequest = {
                            context.openOverlayPermissionSettings()
                        }
                    )

                    PermissionRowItem(
                        iconText = "🎙️",
                        title = "Quyền Micro / Ghi âm",
                        description = "Nhận diện giọng nói để mở khóa màn hình",
                        isGranted = isAudioGranted,
                        onRequest = {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )

                    if (isAtLeastApi33) {
                        PermissionRowItem(
                            iconText = "🔔",
                            title = "Quyền Thông báo",
                            description = "Giúp dịch vụ chạy ngầm liên tục mà không bị dừng",
                            isGranted = isNotificationGranted,
                            onRequest = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }

                    PermissionRowItem(
                        iconText = "⚡",
                        title = "Chạy ngầm & Tiết kiệm pin",
                        description = "Tự động khởi động lại service sau khi tắt nguồn",
                        isGranted = isBatteryGranted,
                        onRequest = {
                            context.openBatteryOptimizationSettings()
                        }
                    )

                    PermissionRowItem(
                        iconText = "🚀",
                        title = "Tự khởi chạy (Auto Start)",
                        description = "Cho phép tự bật lại khi tắt nguồn mở máy (Xiaomi, Oppo, Vivo...)",
                        isGranted = isAutostartGranted,
                        onRequest = {
                            isAutostartClicked = true
                            context.openAutostartSettings()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ButtonApp(
                        text = "Đóng",
                        onClick = onDismissRequest,
                        style = ButtonAppStyle.CANCEL,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ButtonApp(
                        text = "Hoàn Tất",
                        onClick = {
                            if (areMandatoryGranted) {
                                onAllPermissionsGranted()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Vui lòng cấp ít nhất quyền 'Hiển thị trên ứng dụng khác' và 'Micro'!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        style = ButtonAppStyle.OK,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRowItem(
    iconText: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFF1F8E9) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (isGranted) Color(0xFFC8E6C9) else Color(0xFFE0E0E0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isGranted) "✓" else iconText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF2E7D32) else Color(0xFF424242)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFE8F5E9),
                        disabledContentColor = Color(0xFF2E7D32)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✓ Đã cấp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF29B6F6),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Cấp quyền",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
