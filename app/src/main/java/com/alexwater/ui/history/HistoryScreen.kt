package com.alexwater.ui.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexwater.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val month by viewModel.currentMonth.collectAsState()
    val stats by viewModel.monthStats.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val monthLabel = "${month.year}年${month.monthValue}月"
    val now = java.time.YearMonth.now()

    Column(modifier = Modifier.fillMaxSize()) {
        // 月份选择器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousMonth() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Outlined.ChevronLeft, "上个月", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Text(
                monthLabel,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(
                onClick = { viewModel.nextMonth() },
                enabled = month.isBefore(now),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.Outlined.ChevronRight, "下个月",
                    tint = if (month.isBefore(now)) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 三格统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniStatCard("${stats.averageMl}", "日均 (ml)", Modifier.weight(1f))
            MiniStatCard("${stats.goalDaysCount}", "达标天数", Modifier.weight(1f))
            MiniStatCard("${"%.1f".format(stats.totalLiters)}", "本月 (L)", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        // 日期列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(stats.days) { day ->
                DayCard(
                    day = day,
                    goalMl = settings.dailyGoalMl,
                )
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MiniStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DayCard(day: com.alexwater.model.DaySummary, goalMl: Int) {
    var expanded by remember { mutableStateOf(false) }
    val isComplete = day.totalMl >= goalMl
    val dayOfWeek = when (day.date.dayOfWeek.value) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
        5 -> "周五"; 6 -> "周六"; 7 -> "周日"
        else -> ""
    }
    val dateText = "${day.date.year}/${day.date.monthValue.toString().padStart(2, '0')}/${day.date.dayOfMonth.toString().padStart(2, '0')} $dayOfWeek"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dateText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${day.totalMl} ml",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isComplete) Success else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            // 进度条
            LinearProgressIndicator(
                progress = (day.totalMl.toFloat() / goalMl).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (isComplete) Success else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
                strokeCap = StrokeCap.Round,
            )

            Text(
                if (isComplete) "目标 $goalMl ml · ✓ 已达标" else "还差 ${goalMl - day.totalMl} ml",
                fontSize = 12.sp,
                color = if (isComplete) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 展开详情
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))
                    day.records.sortedByDescending { it.timestamp }.forEach { record ->
                        val time = java.time.Instant.ofEpochMilli(record.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(time, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${record.amountMl} ml",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
