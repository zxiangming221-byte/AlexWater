package com.alexwater.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alexwater.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alexwater_settings")

class AppRepository(private val context: Context) {

    // --- Preferences keys ---
    companion object {
        val KEY_DAILY_GOAL = intPreferencesKey("daily_goal_ml")
        val KEY_CUP_SIZE_0 = intPreferencesKey("cup_size_0")
        val KEY_CUP_SIZE_1 = intPreferencesKey("cup_size_1")
        val KEY_CUP_SIZE_2 = intPreferencesKey("cup_size_2")
        val KEY_THEME = intPreferencesKey("theme") // 0=dark, 1=light
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMINDER_PLAN = intPreferencesKey("reminder_plan")
        val KEY_CUSTOM_INTERVAL = intPreferencesKey("custom_interval_minutes")
        val KEY_CUSTOM_TIMES = stringSetPreferencesKey("custom_times")
        val KEY_DND_PERIODS = stringSetPreferencesKey("dnd_periods")
        val KEY_DND_ENABLED = booleanPreferencesKey("dnd_enabled")
    }

    // --- In-memory water records (Room can replace this later) ---
    private val _records = MutableStateFlow<List<WaterRecord>>(mutableListOf())
    val records: StateFlow<List<WaterRecord>> = _records.asStateFlow()

    private var nextId = 1L

    // --- Settings flows ---
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            dailyGoalMl = prefs[KEY_DAILY_GOAL] ?: 2000,
            cupSizes = listOf(
                prefs[KEY_CUP_SIZE_0] ?: 200,
                prefs[KEY_CUP_SIZE_1] ?: 300,
                prefs[KEY_CUP_SIZE_2] ?: 500,
            ),
            theme = when (prefs[KEY_THEME] ?: 0) {
                0 -> Theme.DARK
                else -> Theme.LIGHT
            },
        )
    }

    val reminderConfig: Flow<ReminderConfig> = context.dataStore.data.map { prefs ->
        ReminderConfig(
            enabled = prefs[KEY_REMINDER_ENABLED] ?: true,
            plan = when (prefs[KEY_REMINDER_PLAN] ?: 0) {
                0 -> ReminderPlan.HOURLY
                1 -> ReminderPlan.EVERY_1_5H
                2 -> ReminderPlan.EVERY_2H
                3 -> ReminderPlan.CUSTOM_INTERVAL
                4 -> ReminderPlan.CUSTOM_TIMES
                else -> ReminderPlan.HOURLY
            },
            customIntervalMinutes = prefs[KEY_CUSTOM_INTERVAL] ?: 45,
            customTimes = (prefs[KEY_CUSTOM_TIMES] ?: emptySet())
                .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
                .sortedBy { it }
                .ifEmpty { listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)) },
            dndPeriods = (prefs[KEY_DND_PERIODS] ?: emptySet())
                .mapNotNull { parts ->
                    val split = parts.split("|")
                    if (split.size == 2) {
                        try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                        catch (_: Exception) { null }
                    } else null
                }
                .ifEmpty { listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))) },
            dndEnabled = prefs[KEY_DND_ENABLED] ?: true,
        )
    }

    // --- MaterialTheme.colorScheme.primary record operations ---
    fun addRecord(amountMl: Int): WaterRecord {
        val now = System.currentTimeMillis()
        val record = WaterRecord(
            id = nextId++,
            timestamp = now,
            amountMl = amountMl,
            date = LocalDate.now(),
        )
        _records.value = _records.value + record
        return record
    }

    fun deleteRecord(record: WaterRecord) {
        _records.value = _records.value.filter { it.id != record.id }
    }

    fun getTodayRecords(): List<WaterRecord> {
        val today = LocalDate.now()
        return _records.value.filter { it.date == today }
    }

    fun getRecordsForDate(date: LocalDate): List<WaterRecord> {
        return _records.value.filter { it.date == date }
    }

    fun getRecordsInRange(start: LocalDate, end: LocalDate): List<WaterRecord> {
        return _records.value.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
    }

    fun getAllRecords(): List<WaterRecord> = _records.value

    // --- Settings operations ---
    suspend fun updateDailyGoal(ml: Int) {
        context.dataStore.edit { it[KEY_DAILY_GOAL] = ml }
    }

    suspend fun updateCupSizes(sizes: List<Int>) {
        context.dataStore.edit {
            if (sizes.size > 0) it[KEY_CUP_SIZE_0] = sizes[0]
            if (sizes.size > 1) it[KEY_CUP_SIZE_1] = sizes[1]
            if (sizes.size > 2) it[KEY_CUP_SIZE_2] = sizes[2]
        }
    }

    suspend fun updateTheme(theme: Theme) {
        context.dataStore.edit {
            it[KEY_THEME] = if (theme == Theme.DARK) 0 else 1
        }
    }

    suspend fun updateReminderConfig(config: ReminderConfig) {
        context.dataStore.edit {
            it[KEY_REMINDER_ENABLED] = config.enabled
            it[KEY_REMINDER_PLAN] = when (config.plan) {
                ReminderPlan.HOURLY -> 0
                ReminderPlan.EVERY_1_5H -> 1
                ReminderPlan.EVERY_2H -> 2
                ReminderPlan.CUSTOM_INTERVAL -> 3
                ReminderPlan.CUSTOM_TIMES -> 4
            }
            it[KEY_CUSTOM_INTERVAL] = config.customIntervalMinutes
            it[KEY_CUSTOM_TIMES] = config.customTimes
                .map { t -> t.format(DateTimeFormatter.ofPattern("HH:mm")) }
                .toSet()
            it[KEY_DND_ENABLED] = config.dndEnabled
            it[KEY_DND_PERIODS] = config.dndPeriods
                .map { d -> "${d.start.format(DateTimeFormatter.ofPattern("HH:mm"))}|${d.end.format(DateTimeFormatter.ofPattern("HH:mm"))}" }
                .toSet()
        }
    }
}
