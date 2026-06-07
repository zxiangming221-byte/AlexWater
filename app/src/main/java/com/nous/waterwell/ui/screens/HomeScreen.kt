package com.nous.waterwell.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.waterwell.data.model.DrinkRecord
import com.nous.waterwell.data.model.ReminderTime
import com.nous.waterwell.data.repository.UserPreferences
import com.nous.waterwell.ui.components.WaterProgressRing
import com.nous.waterwell.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    todayTotal: Int,
    todayRecords: List<DrinkRecord>,
    reminderTimes: List<ReminderTime>,
    onLogDrink: (Int, String?) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onResetToday: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val ios = LocalIosColors.current

    // Derived values — computed only when inputs change
    val progress by remember(todayTotal, preferences.targetMl) {
        derivedStateOf { if (preferences.targetMl > 0) todayTotal.toFloat() / preferences.targetMl else 0f }
    }

    var showResetConfirm by remember { mutableStateOf(false) }
    val now = remember { Calendar.getInstance() }
    val greeting = remember(now) {
        when (now.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "夜深了"; in 6..11 -> "早上好"; in 12..13 -> "中午好"; in 14..17 -> "下午好"
            else -> "晚上好"
        }
    }

    // Click debounce — max one drink per 300ms
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val debouncedLogDrink: (Int) -> Unit = remember {
        { amount ->
            val now2 = System.currentTimeMillis()
            if (now2 - lastClickTime > 300) {
                lastClickTime = now2
                onLogDrink(amount, null)
            }
        }
    }

    if (showResetConfirm) AlertDialog(
        onDismissRequest = { showResetConfirm = false },
        title = { Text("重置今日记录", fontWeight = FontWeight.Bold) },
        text = { Text("确定要清除今天所有的喝水记录吗？此操作不可撤销。") },
        confirmButton = { TextButton(onClick = { onResetToday(); showResetConfirm = false }) { Text("确定重置", color = ios.red) } },
        dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("取消") } }
    )

    Scaffold(containerColor = ios.background) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            // Header
            item(key = "header") {
                Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(greeting, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp, color = ios.label))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onNavigateToSchedule) { Icon(Icons.Default.Schedule, "方案", tint = ios.blue, modifier = Modifier.size(24.dp)) }
                            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "设置", tint = ios.blue, modifier = Modifier.size(24.dp)) }
                        }
                    }
                    Text("AlexWater", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, color = ios.secondaryLabel))
                }
            }

            // Progress card
            item(key = "progress") {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ios.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (todayTotal > 0) "已喝水" else "今天还没喝水呢", style = MaterialTheme.typography.bodyLarge.copy(color = ios.secondaryLabel))
                        if (todayTotal > 0) Text("继续加油，保持水分充足 💪", style = MaterialTheme.typography.bodySmall.copy(color = ios.tertiaryLabel))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { WaterProgressRing(progress = progress, currentMl = todayTotal, targetMl = preferences.targetMl) }
                Spacer(Modifier.height(4.dp))
                if (progress >= 1f) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("🎉 目标达成！", style = MaterialTheme.typography.labelLarge.copy(color = ProgressFull, fontWeight = FontWeight.SemiBold))
                }
                if (todayTotal > 0) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = ios.tertiaryLabel)
                        Spacer(Modifier.width(4.dp))
                        Text("重置今日记录", style = MaterialTheme.typography.labelMedium.copy(color = ios.tertiaryLabel))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Quick-add (debounced)
            item(key = "quick_add") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("一杯" to preferences.cupSizeMl, "两杯" to preferences.cupSizeMl * 2, "500ml" to 500).forEach { (label, amount) ->
                        Button(onClick = { debouncedLogDrink(amount) }, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ios.surface, contentColor = ios.blue),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)) { Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Reminder times
            if (reminderTimes.isNotEmpty()) {
                item(key = "reminders") {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ios.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏰", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                                Text("今日提醒", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = ios.label))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                reminderTimes.forEach { t ->
                                    Surface(shape = RoundedCornerShape(8.dp), color = ios.blueLight) {
                                        Text("${String.format("%02d", t.hour)}:${String.format("%02d", t.minute)}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge.copy(color = ios.blue, fontWeight = FontWeight.Medium))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("共 ${reminderTimes.size} 次提醒", style = MaterialTheme.typography.bodySmall.copy(color = ios.tertiaryLabel))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Drink records
            if (todayRecords.isNotEmpty()) {
                item(key = "records_header") {
                    Text("今日记录", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = ios.label, fontSize = 20.sp), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                item(key = "records_list") {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ios.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column {
                            todayRecords.forEachIndexed { i, r ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).clip(CircleShape).background(ios.blueLight), contentAlignment = Alignment.Center) { Text("💧", fontSize = 16.sp) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("${r.amountMl} ml", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, color = ios.label))
                                        Text(formatTime(r.timestamp), style = MaterialTheme.typography.bodySmall.copy(color = ios.secondaryLabel))
                                    }
                                    TextButton(onClick = { onDeleteRecord(r.id) }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("删除", style = MaterialTheme.typography.bodySmall.copy(color = ios.red)) }
                                }
                                if (i < todayRecords.lastIndex) HorizontalDivider(color = ios.separator, modifier = Modifier.padding(start = 56.dp))
                            }
                        }
                    }
                }
            }
            item(key = "spacer") { Spacer(Modifier.height(40.dp)) }
        }
    }
}

private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))
