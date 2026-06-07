package com.nous.waterwell.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nous.waterwell.data.database.AppDatabase
import com.nous.waterwell.data.model.SchedulePresets
import com.nous.waterwell.data.repository.PreferencesManager
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefsManager = PreferencesManager(applicationContext)
        val prefs = prefsManager.preferences.first()

        if (!prefs.remindersEnabled) {
            return Result.success()
        }

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        // Check if within wake hours
        val wakeMinutes = prefs.wakeUpHour * 60 + prefs.wakeUpMinute
        val sleepMinutes = prefs.sleepHour * 60 + prefs.sleepMinute
        val currentMinutes = currentHour * 60 + currentMinute

        if (currentMinutes < wakeMinutes || currentMinutes > sleepMinutes) {
            return Result.success()
        }

        // Check when the last drink was
        val dao = AppDatabase.getInstance(applicationContext).drinkDao()
        val lastRecord = dao.getLatestToday()

        val minutesSinceLastDrink = if (lastRecord != null) {
            ((System.currentTimeMillis() - lastRecord.timestamp) / 60000).toInt()
        } else {
            // No record today — use wake time
            (currentMinutes - wakeMinutes).coerceAtLeast(0)
        }

        // Get active preset to determine reminder times
        val preset = SchedulePresets.ALL.find { it.id == prefs.activePresetId }
            ?: SchedulePresets.ALL.first()

        // Check if current time is within 30 minutes of a scheduled reminder time
        val nearReminderTime = preset.times.any { time ->
            val scheduledMinutes = time.hour * 60 + time.minute
            kotlin.math.abs(currentMinutes - scheduledMinutes) <= 30
        }

        if (nearReminderTime && minutesSinceLastDrink >= 60) {
            NotificationHelper.showReminder(applicationContext, minutesSinceLastDrink)
        } else if (minutesSinceLastDrink >= 120) {
            // Fallback: remind if it's been over 2 hours regardless of schedule
            NotificationHelper.showReminder(applicationContext, minutesSinceLastDrink)
        }

        return Result.success()
    }

}
