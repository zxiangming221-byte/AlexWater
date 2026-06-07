package com.nous.waterwell.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.waterwell.data.model.SchedulePreset
import com.nous.waterwell.ui.theme.*

@Composable
fun ScheduleScreen(
    presets: List<SchedulePreset>,
    activePresetId: String,
    onSelectPreset: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val ios = LocalIosColors.current
    Scaffold(containerColor = ios.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = ios.blue) }
                Text("科学饮水方案", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = ios.label))
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("选择适合你的饮水方案，我们会按方案定时提醒你", style = MaterialTheme.typography.bodyMedium.copy(color = ios.secondaryLabel), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) }
                items(presets) { preset ->
                    val active = preset.id == activePresetId
                    Card(Modifier.fillMaxWidth().clickable { onSelectPreset(preset.id, preset.totalMl) }, shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (active) ios.blue else ios.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(preset.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (active) Color.White else ios.label))
                                if (active) Row(verticalAlignment = Alignment.CenterVertically) { Text("✓", color = Color.White, style = MaterialTheme.typography.labelLarge); Spacer(Modifier.width(4.dp)); Text("当前使用", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium) }
                                else Text("${preset.totalMl}ml/天", style = MaterialTheme.typography.labelMedium.copy(color = ios.secondaryLabel))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(preset.description, style = MaterialTheme.typography.bodyMedium.copy(color = if (active) Color.White.copy(alpha = 0.85f) else ios.secondaryLabel))
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                preset.times.take(8).forEach { t ->
                                    Surface(shape = RoundedCornerShape(6.dp), color = if (active) Color.White.copy(alpha = 0.2f) else ios.blueLight) {
                                        Text("${String.format("%02d", t.hour)}:${String.format("%02d", t.minute)}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(color = if (active) Color.White else ios.blue, fontWeight = FontWeight.Medium))
                                    }
                                }
                                if (preset.times.size > 8) Text("+${preset.times.size - 8}", style = MaterialTheme.typography.labelMedium.copy(color = if (active) Color.White.copy(alpha = 0.5f) else ios.tertiaryLabel), modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}
