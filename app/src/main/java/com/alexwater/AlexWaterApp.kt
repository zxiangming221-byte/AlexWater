package com.alexwater

import android.app.Application
import android.content.Intent
import android.os.Build
import com.alexwater.alarm.AlarmScheduler
import com.alexwater.alarm.CalendarReminderManager
import com.alexwater.alarm.NotificationHelper
import com.alexwater.alarm.WaterReminderService
import com.alexwater.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlexWaterApp : Application() {

    lateinit var repository: AppRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(this)

        // 创建通知渠道
        NotificationHelper.createChannel(this)
        NotificationHelper.createServiceChannel(this)

        // 启动前台保活服务
        startForegroundService()

        // 启动时自动注册闹钟 + 同步日历
        appScope.launch {
            try {
                val config = repository.reminderConfig.first()
                if (config.enabled) {
                    AlarmScheduler.scheduleReminders(this@AlexWaterApp, config)
                }
                CalendarReminderManager.sync(this@AlexWaterApp, config)
            } catch (e: Exception) {
                android.util.Log.e("AlexWaterApp", "Failed to schedule reminders", e)
            }
        }
    }

    private fun startForegroundService() {
        val intent = Intent(this, WaterReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
