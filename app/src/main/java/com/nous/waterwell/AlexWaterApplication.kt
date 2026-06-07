package com.nous.waterwell

import android.app.Application
import com.nous.waterwell.worker.NotificationHelper
import com.nous.waterwell.worker.ReminderScheduler

class AlexWaterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        ReminderScheduler.scheduleReminders(this)
    }
}
