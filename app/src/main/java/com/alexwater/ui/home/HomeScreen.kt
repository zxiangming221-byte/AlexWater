package com.alexwater.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexwater.model.WaterRecord
import com.alexwater.ui.components.ProgressRing
import com.alexwater.ui.components.QuickButton
import com.alexwater.ui.components.WaterBubbles
import com.alexwater.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val todayMl by viewModel.todayMl.collectAsState()
    val records by viewModel.todayRecords.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    val today = java.time.LocalDate.now()
    val dayOfWeek = when (today.dayOfWeek.value) {
        1 -> "星期一"; 2 -> "星期二"; 3 -> "星期三"
        4 -> "星期四"; 5 -> "星期五"; 6 -> "星期六"; 7 -> "星期日"
        else -> ""
    }
    val dateText = "${today.monthValue}月${today.dayOfMonth}日 $dayOfWeek"

    Box(modifier = Modifier.fillMaxSize()) {
        // 浮动气泡
        WaterBubbles(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 日期
            item {
                Spacer(Modifier.height(16.dp))
                Text(dateText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            // 目标文字
            item {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$todayMl",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        " / ${settings.dailyGoalMl} ml",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // 达标横幅
            item {
                AnimatedVisibility(
                    visible = isComplete,
                    enter = slideInVertically() + fadeIn()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessMuted),
                        border = CardDefaults.outlinedCardBorder().copy(
                            // fallback - manual border via modifier
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SuccessMuted, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("✓", color = Success, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("今日目标达成！干得漂亮", color = Success, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 进度环
            item {
                Spacer(Modifier.height(8.dp))
                ProgressRing(
                    progress = progress,
                    isComplete = isComplete,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // 快捷按钮
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    settings.cupSizes.forEach { amount ->
                        QuickButton(
                            amountMl = amount,
                            onClick = { viewModel.addWater(amount) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // 今日记录标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "今日记录",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "共 ${records.size} 次",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // 记录列表
            items(records, key = { it.id }) { record ->
                RecordItem(record, onDelete = { viewModel.deleteRecord(record) })
            }
        }
    }
}

@Composable
private fun RecordItem(record: WaterRecord, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    val timeStr = java.time.Instant.ofEpochMilli(record.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { showDelete = !showDelete },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(timeStr, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${record.amountMl} ml",
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(visible = showDelete) {
                TextButton(onClick = onDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}
