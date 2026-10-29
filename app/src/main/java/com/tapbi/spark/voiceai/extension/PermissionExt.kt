package com.tapbi.spark.voiceai.extension

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasCameraPermission(): Boolean {
    return hasPermission(Manifest.permission.CAMERA)
}

fun Context.hasRecordAudioPermission(): Boolean {
    return hasPermission(Manifest.permission.RECORD_AUDIO)
}

fun Context.hasNotificationPermission(): Boolean {
    return !isAtLeastApi33 || hasPermission(Manifest.permission.POST_NOTIFICATIONS)
}

fun Context.getMissingLockScreenRuntimePermissions(): Array<String> {
    val permissions = mutableListOf<String>()
    if (!hasRecordAudioPermission()) {
        permissions.add(Manifest.permission.RECORD_AUDIO)
    }
    if (isAtLeastApi33 && !hasNotificationPermission()) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions.toTypedArray()
}

fun Context.hasOverlayPermission(): Boolean {
    return try {
        !isAtLeastApi23 || Settings.canDrawOverlays(this)
    } catch (_: Exception) {
        false
    }
}

fun Context.openOverlayPermissionSettings() {
    if (isAtLeastApi23) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(fallbackIntent)
            } catch (_: Exception) {
                openAppPermissionSettings()
            }
        }
    }
}

fun Context.openAppPermissionSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    if (isAtLeastApi23) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: true
    }
    return true
}

fun Context.openBatteryOptimizationSettings() {
    if (isAtLeastApi23) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            openAppPermissionSettings()
        }
    }
}

fun Context.openAutostartSettings() {
    val manufacturer = android.os.Build.MANUFACTURER.lowercase()
    val intents = mutableListOf<Intent>()

    // Xiaomi / Redmi / Poco (MIUI / HyperOS)
    if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        })
        intents.add(Intent("miui.intent.action.OP_AUTO_START").apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartDetailActivity"
            )
        })
    }

    // Oppo / Realme (ColorOS / Realme UI)
    if (manufacturer.contains("oppo") || manufacturer.contains("realme")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.AutoStartActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup10.SmartAutoStartActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startupapp.StartupAppListActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgauge.PowerUsageModelActivity"
            )
        })
    }

    // Vivo / IQOO (FuntouchOS / OriginOS)
    if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.PurifyActivity"
            )
        })
    }

    // Huawei / Honor (EMUI / HarmonyOS)
    if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        })
    }

    // Samsung
    if (manufacturer.contains("samsung")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.ram.AutoRunActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.samsung.android.sm",
                "com.samsung.android.sm.ui.appmanagement.AppManagementActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.samsung.android.looper",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        })
    }

    // Asus
    if (manufacturer.contains("asus")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.autostart.AutoStartActivity"
            )
        })
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity"
            )
        })
    }

    // OnePlus
    if (manufacturer.contains("oneplus")) {
        intents.add(Intent().apply {
            component = android.content.ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
        })
    }

    val pm = packageManager
    for (intent in intents) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resolveInfo = if (isAtLeastApi33) {
            pm.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        if (resolveInfo != null) {
            try {
                startActivity(intent)
                return
            } catch (_: Exception) {
            }
        }
    }

    // Fallback: Open app details page
    openAppPermissionSettings()
}

