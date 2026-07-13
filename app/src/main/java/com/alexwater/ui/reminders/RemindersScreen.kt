package com.alexwater.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderPlan
import com.alexwater.ui.components.SegmentControl
import com.alexwater.ui.components.ToggleSwitch
import com.alexwater.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val config by viewModel.config.collectAsState()
    val scrollState = rememberScrollState()

    // Time picker state — two-step for DND
    var pickerMode by remember { mutableStateOf("") } // "" = hidden, "time" = custom, "dnd_start", "dnd_end", "dnd_edit_start", "dnd_edit_end"
    var pendingDndStart by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var pendingDndOld by remember { mutableStateOf<DndPeriod?>(null) }

    if (pickerMode.isNotEmpty()) {
        val now = LocalTime.now()
        val initialTime = if (pickerMode.contains("end")) pendingDndStart.plusHours(1) else now
        val state = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = (initialTime.minute / 5) * 5,
            is24Hour = true,
        )
        val isStart = pickerMode.contains("start")
        val isEdit = pickerMode.contains("edit")

        AlertDialog(
            onDismissRequest = { pickerMode = "" },
            title = {
                Text(
                    when {
                        isEdit && isStart -> "修改开始时间"
                        isEdit -> "修改结束时间"
                        isStart -> "选择开始时间"
                        else -> "选择结束时间"
                    },
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                TimePicker(state = state, modifier = Modifier.padding(horizontal = 8.dp))
            },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(state.hour, state.minute)
                    when (pickerMode) {
                        "time" -> {
                            viewModel.addCustomTime(time)
                            pickerMode = ""
                        }
                        "dnd_start" -> {
                            pendingDndStart = time
                            pickerMode = "dnd_end"
                        }
                        "dnd_end" -> {
                            viewModel.addDndPeriod(DndPeriod(pendingDndStart, time))
                            pickerMode = ""
                        }
                        "dnd_edit_start" -> {
                            pendingDndStart = time
                            pickerMode = "dnd_edit_end"
                        }
                        "dnd_edit_end" -> {
                            pendingDndOld?.let { old ->
                                viewModel.editDndPeriod(old, DndPeriod(pendingDndStart, time))
                            }
                            pickerMode = ""
                        }
                    }
                }) {
                    Text(if (pickerMode.endsWith("end")) "确定添加" else "下一步", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { pickerMode = "" }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 全局开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("提醒开关", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ToggleSwitch(checked = config.enabled, onCheckedChange = { viewModel.updateEnabled(it) })
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("提醒方案")
        Spacer(Modifier.height(8.dp))

        val planOptions = listOf("每小时", "每 1.5 小时", "每 2 小时")
        val planIndex = when (config.plan) {
            ReminderPlan.HOURLY -> 0
            ReminderPlan.EVERY_1_5H -> 1
            ReminderPlan.EVERY_2H -> 2
            ReminderPlan.CUSTOM_INTERVAL, ReminderPlan.CUSTOM_TIMES -> -1
        }

        if (planIndex >= 0) {
            SegmentControl(
                options = planOptions,
                selectedIndex = planIndex,
                onSelect = { index ->
                    viewModel.updatePlan(when (index) {
                        0 -> ReminderPlan.HOURLY
                        1 -> ReminderPlan.EVERY_1_5H
                        2 -> ReminderPlan.EVERY_2H
                        else -> ReminderPlan.HOURLY
                    })
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        // 自定义选项卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 自定义间隔 toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("使用自定义间隔", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ToggleSwitch(
                        checked = config.plan == ReminderPlan.CUSTOM_INTERVAL,
                        onCheckedChange = { checked ->
                            viewModel.updatePlan(if (checked) ReminderPlan.CUSTOM_INTERVAL else ReminderPlan.HOURLY)
                        }
                    )
                }
                if (config.plan == ReminderPlan.CUSTOM_INTERVAL) {
                    Spacer(Modifier.height(12.dp))
                    Text("每隔 ${config.customIntervalMinutes} 分钟提醒一次",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = config.customIntervalMinutes.toFloat(),
                        onValueChange = { viewModel.updateCustomInterval(it.toInt()) },
                        valueRange = 15f..240f, steps = 44,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                // 自定义时间点 toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("使用自定义时间点", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ToggleSwitch(
                        checked = config.plan == ReminderPlan.CUSTOM_TIMES,
                        onCheckedChange = { checked ->
                            viewModel.updatePlan(if (checked) ReminderPlan.CUSTOM_TIMES else ReminderPlan.HOURLY)
                        }
                    )
                }

                if (config.plan == ReminderPlan.CUSTOM_TIMES) {
                    Spacer(Modifier.height(12.dp))
                    if (config.customTimes.isEmpty()) {
                        Text("尚未添加提醒时间", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        config.customTimes.forEach { time ->
                            TimeSlotChip(
                                time = time,
                                onRemove = { viewModel.removeCustomTime(time) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                    TextButton(onClick = {
                        pickerMode = "time"
                    }) {
                        Text("+ 添加时间", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 免打扰时段
        SectionTitle("免打扰时段")
        Spacer(Modifier.height(8.dp))

        // DND 总开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("启用免打扰", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ToggleSwitch(
                    checked = config.dndEnabled,
                    onCheckedChange = { viewModel.updateDndEnabled(it) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("在这些时段内暂停提醒", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                if (config.dndPeriods.isEmpty()) {
                    Text("未设置免打扰时段", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    config.dndPeriods.forEach { period ->
                        DndSlotChip(
                            period = period,
                            onClick = {
                                pendingDndOld = period
                                pendingDndStart = period.start
                                pickerMode = "dnd_edit_start"
                            },
                            onRemove = { viewModel.removeDndPeriod(period) }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }

                TextButton(onClick = {
                    pickerMode = "dnd_start"
                }) {
                    Text("+ 添加时段", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 通知预览
        SectionTitle("通知预览")
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💧 该喝水了", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Text("你已经 1.5 小时没喝水了，补充一下水分吧",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(200, 300, 500).forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+$amount ml", color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun TimeSlotChip(time: LocalTime, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            time.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("提醒", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Outlined.Close, "删除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DndSlotChip(period: DndPeriod, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            period.start.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(
            period.end.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Outlined.Close, "删除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp))
}
