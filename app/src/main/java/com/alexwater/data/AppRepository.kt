package com.alexwater.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alexwater.data.db.AppDatabase
import com.alexwater.data.db.WaterRecordEntity
import com.alexwater.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alexwater_settings")
private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

class AppRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao get() = db.waterRecordDao()

    // ===== Settings =====

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            dailyGoalMl = p[K.dailyGoal] ?: 2000,
            cupSizes = listOf(p[K.cup0] ?: 200, p[K.cup1] ?: 300, p[K.cup2] ?: 500),
            theme = if ((p[K.theme] ?: 0) == 1) Theme.LIGHT else Theme.DARK,
        )
    }

    val reminderConfig: Flow<ReminderConfig> = context.dataStore.data.map { p ->
        ReminderConfig(
            enabled = p[K.remEnabled] ?: true,
            plan = when (p[K.remPlan] ?: 0) {
                0 -> ReminderPlan.HOURLY; 1 -> ReminderPlan.EVERY_1_5H; 2 -> ReminderPlan.EVERY_2H
                3 -> ReminderPlan.CUSTOM_INTERVAL; 4 -> ReminderPlan.CUSTOM_TIMES
                else -> ReminderPlan.HOURLY
            },
            customIntervalMinutes = p[K.remInterval] ?: 45,
            customTimes = (p[K.remTimes] ?: emptySet())
                .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
                .sortedBy { it }
                .ifEmpty { listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)) },
            dndEnabled = p[K.dndOn] ?: true,
            dndPeriods = (p[K.dndList] ?: emptySet())
                .mapNotNull { s ->
                    val parts = s.split("|")
                    if (parts.size == 2) try { DndPeriod(LocalTime.parse(parts[0]), LocalTime.parse(parts[1])) } catch (_: Exception) { null }
                    else null
                }
                .ifEmpty { listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))) },
        )
    }

    // ===== Records (Room) =====

    val records: Flow<List<WaterRecord>> = dao.getAll().map { list -> list.map { it.toModel() } }

    fun getRecordsByDate(date: LocalDate): Flow<List<WaterRecord>> =
        dao.getByDate(date.format(dateFmt)).map { list -> list.map { it.toModel() } }

    suspend fun addRecord(amountMl: Int) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            dao.insert(WaterRecordEntity(timestamp = now, amountMl = amountMl, date = LocalDate.now().format(dateFmt)))
        }
    }

    suspend fun deleteRecord(record: WaterRecord) {
        withContext(Dispatchers.IO) {
            dao.delete(WaterRecordEntity(record.id, record.timestamp, record.amountMl, record.date.format(dateFmt)))
        }
    }

    // ===== Settings write =====

    suspend fun updateDailyGoal(ml: Int) { context.dataStore.edit { it[K.dailyGoal] = ml } }
    suspend fun updateCupSizes(s: List<Int>) {
        context.dataStore.edit {
            if (s.size > 0) it[K.cup0] = s[0]
            if (s.size > 1) it[K.cup1] = s[1]
            if (s.size > 2) it[K.cup2] = s[2]
        }
    }
    suspend fun updateTheme(t: Theme) { context.dataStore.edit { it[K.theme] = if (t == Theme.DARK) 0 else 1 } }
    suspend fun updateReminderConfig(c: ReminderConfig) {
        context.dataStore.edit {
            it[K.remEnabled] = c.enabled
            it[K.remPlan] = when (c.plan) {
                ReminderPlan.HOURLY -> 0; ReminderPlan.EVERY_1_5H -> 1; ReminderPlan.EVERY_2H -> 2
                ReminderPlan.CUSTOM_INTERVAL -> 3; ReminderPlan.CUSTOM_TIMES -> 4
            }
            it[K.remInterval] = c.customIntervalMinutes
            it[K.remTimes] = c.customTimes.map { t -> t.format(DateTimeFormatter.ofPattern("HH:mm")) }.toSet()
            it[K.dndOn] = c.dndEnabled
            it[K.dndList] = c.dndPeriods.map { d -> "${d.start.format(DateTimeFormatter.ofPattern("HH:mm"))}|${d.end.format(DateTimeFormatter.ofPattern("HH:mm"))}" }.toSet()
        }
    }

    // ===== Keys =====
    private object K {
        val dailyGoal = intPreferencesKey("daily_goal_ml")
        val cup0 = intPreferencesKey("cup_size_0")
        val cup1 = intPreferencesKey("cup_size_1")
        val cup2 = intPreferencesKey("cup_size_2")
        val theme = intPreferencesKey("theme")
        val remEnabled = booleanPreferencesKey("reminder_enabled")
        val remPlan = intPreferencesKey("reminder_plan")
        val remInterval = intPreferencesKey("custom_interval_minutes")
        val remTimes = stringSetPreferencesKey("custom_times")
        val dndOn = booleanPreferencesKey("dnd_enabled")
        val dndList = stringSetPreferencesKey("dnd_periods")
    }
}

private fun WaterRecordEntity.toModel() = WaterRecord(id, timestamp, amountMl, LocalDate.parse(date, dateFmt))
