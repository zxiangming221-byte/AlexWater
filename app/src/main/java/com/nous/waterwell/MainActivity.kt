package com.nous.waterwell

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nous.waterwell.data.model.SchedulePresets
import com.nous.waterwell.ui.components.SplashScreen
import com.nous.waterwell.ui.screens.HomeScreen
import com.nous.waterwell.ui.screens.ScheduleScreen
import com.nous.waterwell.ui.screens.SettingsScreen
import com.nous.waterwell.ui.theme.AccentPresets
import com.nous.waterwell.ui.theme.AlexWaterTheme
import com.nous.waterwell.viewmodel.WaterViewModel
import com.nous.waterwell.worker.ReminderForegroundService
import com.nous.waterwell.worker.ReminderScheduler

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) startReminderService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else startReminderService()
        } else startReminderService()
        setContent { AlexWaterApp() }
    }

    private fun startReminderService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(Intent(this, ReminderForegroundService::class.java))
        ReminderScheduler.scheduleReminders(this)
    }
}

@Composable
fun AlexWaterApp() {
    var showSplash by remember { mutableStateOf(true) }
    val viewModel: WaterViewModel = viewModel()
    val navController = rememberNavController()
    val preferences by viewModel.preferences.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val accent = AccentPresets.getOrElse(preferences.accentColorIndex) { AccentPresets[0] }
    val activePreset = SchedulePresets.ALL.find { it.id == preferences.activePresetId } ?: SchedulePresets.ALL.first()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshToday()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize()) {
        AlexWaterTheme(darkTheme = preferences.darkMode, accentColor = accent.primary, language = preferences.languageCode) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(preferences = preferences, todayTotal = todayTotal, todayRecords = todayRecords,
                        reminderTimes = activePreset.times,
                        onLogDrink = { a, n -> viewModel.logDrink(a, n) },
                        onDeleteRecord = { viewModel.deleteRecord(it) },
                        onResetToday = { viewModel.resetToday() },
                        onNavigateToSchedule = { navController.navigate("schedule") },
                        onNavigateToSettings = { navController.navigate("settings") })
                }
                composable("schedule") {
                    ScheduleScreen(presets = viewModel.allPresets, activePresetId = preferences.activePresetId,
                        onSelectPreset = { id, ml -> viewModel.selectPreset(id); viewModel.updateTargetMl(ml); ReminderScheduler.scheduleReminders(navController.context) },
                        onBack = { navController.popBackStack() })
                }
                composable("settings") {
                    SettingsScreen(preferences = preferences,
                        onUpdateTargetMl = { viewModel.updateTargetMl(it) },
                        onUpdateWakeTime = { h, m -> viewModel.updateWakeTime(h, m) },
                        onUpdateSleepTime = { h, m -> viewModel.updateSleepTime(h, m) },
                        onUpdateCupSize = { viewModel.updateCupSize(it) },
                        onSetRemindersEnabled = { viewModel.setRemindersEnabled(it) },
                        onSetVibrationEnabled = { viewModel.setVibrationEnabled(it) },
                        onSetSoundEnabled = { viewModel.setSoundEnabled(it) },
                        onSetDarkMode = { viewModel.setDarkMode(it) },
                        onSetAccentColorIndex = { viewModel.setAccentColorIndex(it) },
                        onSetLanguage = { viewModel.setLanguageCode(it) },
                        onBack = { navController.popBackStack() })
                }
            }
        }
        if (showSplash) SplashScreen(onFinished = { showSplash = false })
    }
}
