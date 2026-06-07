package com.nous.waterwell.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.waterwell.R
import com.nous.waterwell.data.AppLanguage
import com.nous.waterwell.data.repository.UserPreferences
import com.nous.waterwell.ui.components.FullScreenQrDialog
import com.nous.waterwell.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences, onUpdateTargetMl: (Int) -> Unit, onUpdateWakeTime: (Int, Int) -> Unit,
    onUpdateSleepTime: (Int, Int) -> Unit, onUpdateCupSize: (Int) -> Unit,
    onSetRemindersEnabled: (Boolean) -> Unit, onSetVibrationEnabled: (Boolean) -> Unit,
    onSetSoundEnabled: (Boolean) -> Unit, onSetDarkMode: (Boolean) -> Unit,
    onSetAccentColorIndex: (Int) -> Unit, onSetLanguage: (String) -> Unit, onBack: () -> Unit
) {
    val ios = LocalIosColors.current
    val s = LocalStrings.current
    var showFullQr by remember { mutableStateOf(false) }
    if (showFullQr) FullScreenQrDialog(onDismiss = { showFullQr = false })

    Scaffold(containerColor = ios.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ios.blue) }
                Text(s.settingsTitle, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = ios.label))
            }
            Spacer(Modifier.height(8.dp))

            // Daily Goal
            SectionHeader(s.dailyGoal); GroupCard {
                var t by rememberSaveable { mutableStateOf(preferences.targetMl.toString()) }
                InputRow(t, { v -> val f = v.filter { it.isDigit() }; t = f; (f.toIntOrNull() ?: return@InputRow).let { if (it in 100..9999) onUpdateTargetMl(it) } }, s.dailyGoalLabel, s.ml)
                HorizontalDivider(color = ios.separator, modifier = Modifier.padding(start = 16.dp))
                ChipRow(listOf(1500, 2000, 2500, 3000), preferences.targetMl, s.ml) { t = it.toString(); onUpdateTargetMl(it) }
            }
            Spacer(Modifier.height(16.dp))

            // Cup Size
            SectionHeader(s.cupSize); GroupCard {
                var c by rememberSaveable { mutableStateOf(preferences.cupSizeMl.toString()) }
                InputRow(c, { v -> val f = v.filter { it.isDigit() }; c = f; (f.toIntOrNull() ?: return@InputRow).let { if (it in 50..2000) onUpdateCupSize(it) } }, s.cupSizeLabel, s.ml)
                HorizontalDivider(color = ios.separator, modifier = Modifier.padding(start = 16.dp))
                ChipRow(listOf(150, 200, 250, 300, 500), preferences.cupSizeMl, s.ml) { c = it.toString(); onUpdateCupSize(it) }
            }
            Spacer(Modifier.height(16.dp))

            // Schedule
            SectionHeader(s.wakeSleep); GroupCard {
                TimeRow(s.wakeLabel, preferences.wakeUpHour, preferences.wakeUpMinute, onUpdateWakeTime)
                HorizontalDivider(color = ios.separator, modifier = Modifier.padding(start = 16.dp))
                TimeRow(s.sleepLabel, preferences.sleepHour, preferences.sleepMinute, onUpdateSleepTime)
                HorizontalDivider(color = ios.separator, modifier = Modifier.padding(start = 16.dp))
                Text(s.wakeSleepDesc, style = MaterialTheme.typography.bodySmall.copy(color = ios.tertiaryLabel), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
            Spacer(Modifier.height(16.dp))

            // Notifications
            SectionHeader(s.notifications); GroupCard {
                SwitchRow(s.remindersSwitch, preferences.remindersEnabled, onSetRemindersEnabled)
                Div(); SwitchRow(s.vibrationSwitch, preferences.vibrationEnabled, onSetVibrationEnabled)
                Div(); SwitchRow(s.soundSwitch, preferences.soundEnabled, onSetSoundEnabled)
            }
            Spacer(Modifier.height(16.dp))

            // Appearance
            SectionHeader(s.appearance); GroupCard {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (preferences.darkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = if (preferences.darkMode) ios.orange else ios.label, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(if (preferences.darkMode) s.darkMode else s.lightMode, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label), modifier = Modifier.weight(1f))
                    Switch(preferences.darkMode, onSetDarkMode, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ios.blue))
                }
                Div()
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(s.accentLabel, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label))
                    Spacer(Modifier.height(10.dp))
                    @OptIn(ExperimentalLayoutApi::class) FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccentPresets.forEachIndexed { idx, acc ->
                            val sel = idx == preferences.accentColorIndex
                            Box(Modifier.size(36.dp).clip(CircleShape).background(acc.primary).clickable { onSetAccentColorIndex(idx) }, contentAlignment = Alignment.Center) {
                                if (sel) Text("\u2713", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Div()
                LanguageRow(preferences.languageCode, onSetLanguage)
            }
            Spacer(Modifier.height(24.dp))

            // About
            SectionHeader(s.about); GroupCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val inf = rememberInfiniteTransition("coffee")
                    val so by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), "steam")
                    val bo by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), "bounce")
                    Canvas(Modifier.size(64.dp)) {
                        val w = size.width; val h = size.height; val bp = bo * 3f; val cw = w * 0.6f; val ch = h * 0.5f
                        val cl = (w - cw) / 2f; val ct = h * 0.32f - bp
                        val sa = if (so < 0.5f) so * 2f else (1f - so) * 2f
                        for (i in 0..2) { val sx = w / 2f + (i - 1) * cw * 0.22f; drawLine(ios.tertiaryLabel.copy(alpha = sa * 0.5f), Offset(sx, ct - 4f - so * 12f), Offset(sx + (i - 1) * 3f, ct - 12f - so * 12f), 1.5f, cap = StrokeCap.Round) }
                        drawRoundRect(ios.blue, Offset(cl, ct), Size(cw, ch), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                        drawArc(ios.blue, -20f, 220f, false, Offset(cl + cw - 3f, ct + ch * 0.18f), Size(cw * 0.25f, ch * 0.45f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                        drawLine(Color.White.copy(alpha = 0.3f), Offset(cl + 6f, ct + ch * 0.28f), Offset(cl + cw - 6f, ct + ch * 0.28f), 1.2f)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(s.aboutTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = ios.label), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text(s.aboutVersion, style = MaterialTheme.typography.bodySmall.copy(color = ios.secondaryLabel), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Text(s.aboutMessage, style = MaterialTheme.typography.bodyMedium.copy(color = ios.secondaryLabel, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Text(s.aboutTapQr, style = MaterialTheme.typography.labelMedium.copy(color = ios.tertiaryLabel), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Image(painter = painterResource(R.drawable.donate_qr), contentDescription = null, modifier = Modifier.fillMaxWidth(0.55f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).clickable { showFullQr = true }, contentScale = ContentScale.Fit)
                    Spacer(Modifier.height(6.dp))
                    Text(s.aboutScanQr, style = MaterialTheme.typography.labelMedium.copy(color = ios.tertiaryLabel), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable private fun LanguageRow(current: String, onSet: (String) -> Unit) {
    val ios = LocalIosColors.current; val s = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(s.languageLabel, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label), modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { expanded = true }) { Text(AppLanguage.fromCode(current).label, color = ios.blue) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AppLanguage.entries.forEach { lang ->
                    DropdownMenuItem(text = { Text(lang.label) }, onClick = { onSet(lang.code); expanded = false })
                }
            }
        }
    }
}

@Composable private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge.copy(color = LocalIosColors.current.secondaryLabel, fontWeight = FontWeight.Medium), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
}
@Composable private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    val ios = LocalIosColors.current
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ios.surface), elevation = CardDefaults.cardElevation(0.dp)) { Column(content = content) }
}
@Composable private fun InputRow(value: String, onChange: (String) -> Unit, label: String, suffix: String) {
    val ios = LocalIosColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label), modifier = Modifier.width(80.dp))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(value, onChange, Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text(suffix, color = ios.tertiaryLabel) }, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ios.blue, unfocusedBorderColor = ios.separator, focusedLabelColor = ios.blue, cursorColor = ios.blue))
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun ChipRow(values: List<Int>, selected: Int, suffix: String, onSelect: (Int) -> Unit) {
    val ios = LocalIosColors.current
    FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { v -> FilterChip(selected = selected == v, onClick = { onSelect(v) }, label = { Text("$v$suffix") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ios.blue.copy(alpha = 0.12f), selectedLabelColor = ios.blue), shape = RoundedCornerShape(8.dp), border = FilterChipDefaults.filterChipBorder(borderColor = ios.separator, selectedBorderColor = ios.blue, enabled = true, selected = selected == v)) }
    }
}
@Composable private fun TimeRow(label: String, hour: Int, minute: Int, onChanged: (Int, Int) -> Unit) {
    val ios = LocalIosColors.current; var he by remember { mutableStateOf(false) }; var me by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label), modifier = Modifier.width(80.dp)); Spacer(Modifier.weight(1f))
        Box { TextButton(onClick = { he = true }) { Text("${String.format("%02d", hour)}", color = ios.blue, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) }
            DropdownMenu(expanded = he, onDismissRequest = { he = false }) { (0..23).forEach { h -> DropdownMenuItem(text = { Text("${String.format("%02d", h)}h") }, onClick = { onChanged(h, minute); he = false }) } } }
        Text(":", color = ios.label)
        Box { TextButton(onClick = { me = true }) { Text("${String.format("%02d", minute)}", color = ios.blue, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) }
            DropdownMenu(expanded = me, onDismissRequest = { me = false }) { (0..59 step 5).forEach { m -> DropdownMenuItem(text = { Text("${String.format("%02d", m)}m") }, onClick = { onChanged(hour, m); me = false }) } } }
    }
}
@Composable private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val ios = LocalIosColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = ios.label), modifier = Modifier.weight(1f))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ios.blue))
    }
}
@Composable private fun Div() { HorizontalDivider(color = LocalIosColors.current.separator, modifier = Modifier.padding(start = 16.dp)) }
