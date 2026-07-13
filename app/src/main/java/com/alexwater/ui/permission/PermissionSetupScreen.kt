package com.alexwater.ui.permission

import android.Manifest
import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alexwater.ui.theme.*

@Composable
fun PermissionSetupScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity ?: return

    // 每个权限独立 state
    var alarmGranted by remember { mutableStateOf(checkExactAlarm(context)) }
    var notifyGranted by remember { mutableStateOf(checkNotification(context)) }
    var batteryGranted by remember { mutableStateOf(checkBatteryOptimization(context)) }
    var calendarGranted by remember { mutableStateOf(checkCalendar(context)) }

    // 从系统设置返回后自动刷新
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                alarmGranted = checkExactAlarm(context)
                notifyGranted = checkNotification(context)
                batteryGranted = checkBatteryOptimization(context)
                calendarGranted = checkCalendar(context)
            }
        }
        (activity as LifecycleOwner).lifecycle.addObserver(observer)
        onDispose { (activity as LifecycleOwner).lifecycle.removeObserver(observer) }
    }

    val allGranted = alarmGranted && notifyGranted && batteryGranted && calendarGranted

    LaunchedEffect(allGranted) {
        if (allGranted) onAllGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))

        Icon(
            Icons.Outlined.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Water,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "让 AlexWater 准时提醒你",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "需要开启以下权限，提醒才能正常运作",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        // 精确闹钟
        PermissionCard(
            icon = Icons.Outlined.Alarm,
            title = "精确闹钟权限",
            description = "让提醒准时响起，即使在省电模式下也不会延迟",
            granted = alarmGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    })
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // 通知
        PermissionCard(
            icon = Icons.Outlined.Notifications,
            title = "通知权限",
            description = "在通知栏显示喝水提醒和快捷记录按钮",
            granted = notifyGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // 电池优化
        PermissionCard(
            icon = Icons.Outlined.BatterySaver,
            title = "忽略电池优化",
            description = "防止手机在后台自动关闭提醒服务",
            granted = batteryGranted,
            onRequest = {
                try {
                    // 方式1：直接弹出系统授权对话框（需要 REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 权限）
                    activity.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    })
                } catch (_: Exception) {
                    // 方式2：降级到电池优化列表页（所有设备通用）
                    try {
                        activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (_: Exception) {
                        // 方式3：最后的降级 — 打开应用详情页
                        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        })
                    }
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // 日历
        PermissionCard(
            icon = Icons.Outlined.CalendarMonth,
            title = "日历权限",
            description = "接入系统日历，即使 APP 关闭也能准时提醒",
            granted = calendarGranted,
            onRequest = {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1002
                )
            }
        )
        Spacer(Modifier.height(32.dp))

        if (allGranted) {
            Button(
                onClick = onAllGranted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("开始使用", fontSize = 16.sp)
            }
        } else {
            Text(
                "请完成以上授权后继续",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) SuccessMuted
            else MaterialTheme.colorScheme.surface
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(32.dp),
                tint = if (granted) Success else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(description, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
            if (granted) {
                Icon(Icons.Outlined.CheckCircle, "已授权", Modifier.size(28.dp), tint = Success)
            } else {
                Button(onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) { Text("去开启", fontSize = 13.sp) }
            }
        }
    }
}

// 权限检查函数（纯函数，可复用）
private fun checkExactAlarm(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    return am.canScheduleExactAlarms()
}

private fun checkNotification(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkBatteryOptimization(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun checkCalendar(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
}
