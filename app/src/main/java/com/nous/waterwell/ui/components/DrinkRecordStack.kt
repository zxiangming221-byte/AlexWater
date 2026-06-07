package com.nous.waterwell.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.waterwell.data.model.DrinkRecord
import com.nous.waterwell.ui.theme.LocalIosColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DrinkRecordStack(records: List<DrinkRecord>, onDelete: (Long) -> Unit, modifier: Modifier = Modifier) {
    if (records.isEmpty()) return
    val ios = LocalIosColors.current
    val visibleCards = records.take(4).reversed()
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Box(modifier = modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        visibleCards.forEachIndexed { index, record ->
            val isTopCard = index == 0
            val isExpanded = index == expandedIndex
            val baseOffsetY = if (isTopCard) 0f else index * 12f
            val baseOffsetX = if (isTopCard) 0f else index * 8f
            val baseRotation = if (isTopCard) 0f else (index * 2.5f - 1f)

            val expandOffset by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "expandX"
            )

            Box(
                modifier = Modifier.fillMaxWidth(0.85f)
                    .graphicsLayer {
                        translationY = baseOffsetY.dp.toPx()
                        translationX = baseOffsetX.dp.toPx() + expandOffset
                        rotationZ = baseRotation
                        shadowElevation = (6f - index * 1.5f).coerceAtLeast(2f)
                        val scale = 1f - index * 0.03f
                        scaleX = scale; scaleY = scale
                    }
                    .pointerInput(record.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = { if (isTopCard) expandedIndex = if (isExpanded) -1 else 0 }
                        ) { _, _ -> }
                    }
            ) {
                FrostedGlassCard(cornerRadius = 16.dp) {
                    DrinkCardContent(record, isExpanded, { onDelete(record.id) }, { expandedIndex = -1 })
                }
            }
        }
    }
}

@Composable
private fun DrinkCardContent(record: DrinkRecord, isExpanded: Boolean, onDelete: () -> Unit, onCollapse: () -> Unit) {
    val ios = LocalIosColors.current
    val timeStr = remember(record.timestamp) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp)) }
    val minutesAgo = remember(record.timestamp) { ((System.currentTimeMillis() - record.timestamp) / 60000).toInt() }
    val timeAgoStr = when { minutesAgo < 1 -> "刚刚"; minutesAgo < 60 -> "${minutesAgo}分钟前"; else -> timeStr }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(ios.blueLight), contentAlignment = Alignment.Center) { Text("💧", fontSize = 24.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("${record.amountMl} ml", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ios.label, fontSize = 26.sp))
                Text(timeAgoStr, style = MaterialTheme.typography.bodySmall.copy(color = ios.secondaryLabel))
            }
            repeat((record.amountMl / 250).coerceAtMost(3)) { i ->
                Text("🥤", fontSize = if (i == 0) 20.sp else 16.sp, modifier = Modifier.padding(start = if (i > 0) (-6).dp else 0.dp))
            }
        }
        if (isExpanded) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCollapse) { Text("收起", color = ios.secondaryLabel) }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("删除", color = ios.red) }
            }
        } else {
            Spacer(Modifier.height(4.dp))
            Text("← 滑动查看操作", style = MaterialTheme.typography.labelMedium.copy(color = ios.tertiaryLabel, fontSize = 10.sp))
        }
    }
}
