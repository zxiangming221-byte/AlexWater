package com.alexwater

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.alexwater.model.Theme
import com.alexwater.ui.navigation.AlexWaterApp
import com.alexwater.ui.permission.PermissionSetupScreen
import com.alexwater.ui.theme.AlexWaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AlexWaterApp
        setContent {
            val settings by app.repository.settings.collectAsState(
                initial = com.alexwater.model.AppSettings()
            )
            AlexWaterTheme(darkTheme = settings.theme == Theme.DARK) {
                var showPermissionSetup by remember {
                    mutableStateOf(!allPermissionsGranted())
                }
                if (showPermissionSetup) {
                    PermissionSetupScreen(
                        onAllGranted = { showPermissionSetup = false }
                    )
                } else {
                    AlexWaterApp()
                }
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        // 精确闹钟权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) return false
        }
        // 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        }
        // 电池优化
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) return false
        // 日历权限（推荐但非强制）
        // 日历是可选的，不影响基本功能
        return true
    }
}
