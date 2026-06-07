package com.nous.waterwell.worker

import android.content.Context
import androidx.work.*
import com.nous.waterwell.data.database.AppDatabase
import com.nous.waterwell.data.model.SchedulePreset
import com.nous.waterwell.data.model.SchedulePresets
import com.nous.waterwell.data.repository.PreferencesManager
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME_PERIODIC = "waterwell_periodic_reminder"

        fun scheduleReminders(context: Context) {
            ReminderScheduler(context).schedule()
        }

        fun cancelReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
        }
    }

    fun schedule() {
        val prefsManager = PreferencesManager(context)

        // Build a OneTimeWorkRequest chain for each reminder time,
        // then a periodic fallback for every 60 minutes

        // Periodic check every 60 minutes as a safety net
        val periodicRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            60, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }
}
