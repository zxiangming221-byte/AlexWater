package com.alexwater.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexwater.ui.components.BarChart
import com.alexwater.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val stats by viewModel.weekStats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val weekLabels by viewModel.weekLabels.collectAsState()
    val offset by viewModel.weekOffset.collectAsState()
    val scrollState = rememberScrollState()

    val weekTabs = listOf("本周", "上周")
    val weekTabOffsets = listOf(0, -1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 周选择器
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            weekTabs.forEachIndexed { index, label ->
                val selected = offset == weekTabOffsets[index]
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setWeek(weekTabOffsets[index]) },
                    label = { Text(label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 三格统计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("${stats.averageMl}", "日均 (ml)", Modifier.weight(1f))
            StatCard("${stats.bestMl}", "最佳日 (ml)", Modifier.weight(1f))
            StatCard("${stats.goalDaysCount}/7", "达标天数", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // 柱状图
        SectionTitle("每日摄入")
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val barData = stats.days.mapIndexed { i, day ->
                    weekLabels.getOrElse(i) { "" } to day.totalMl
                }
                BarChart(
                    data = barData,
                    goalMl = settings.dailyGoalMl,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 趋势分析
        SectionTitle("趋势分析")
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TrendRow("7 日均值", "${stats.averageMl} ml", trend = "↑ 5%", trendUp = true)
                TrendRow("本周最高", "${stats.bestMl} ml")
                TrendRow("本周最低", "${stats.days.minOfOrNull { it.totalMl } ?: 0} ml")
                TrendRow("摄入波动", "±${((stats.bestMl - (stats.days.minOfOrNull { it.totalMl } ?: 0)) / 2)} ml")
            }
        }

        Spacer(Modifier.height(20.dp))

        // 时段分布
        SectionTitle("时段分布")
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 简化：基于所有记录计算时段分布
                val allRecords = stats.days.flatMap { it.records }
                val morning = allRecords.count { r ->
                    val h = java.time.Instant.ofEpochMilli(r.timestamp).atZone(java.time.ZoneId.systemDefault()).hour
                    h in 6..11
                }
                val afternoon = allRecords.count { r ->
                    val h = java.time.Instant.ofEpochMilli(r.timestamp).atZone(java.time.ZoneId.systemDefault()).hour
                    h in 12..17
                }
                val evening = allRecords.count { r ->
                    val h = java.time.Instant.ofEpochMilli(r.timestamp).atZone(java.time.ZoneId.systemDefault()).hour
                    h in 18..23
                }
                val total = (morning + afternoon + evening).coerceAtLeast(1)
                DistributionRow("上午 (6–12)", morning * 100 / total, Modifier.fillMaxWidth())
                DistributionRow("下午 (12–18)", afternoon * 100 / total, Modifier.fillMaxWidth())
                DistributionRow("晚上 (18–24)", evening * 100 / total, Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrendRow(label: String, value: String, trend: String = "", trendUp: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            if (trend.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    trend,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (trendUp) Success else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DistributionRow(label: String, percentage: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$percentage%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
