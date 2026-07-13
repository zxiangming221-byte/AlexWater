package com.alexwater.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexwater.AlexWaterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? AlexWaterApp ?: return
            scope.launch {
                try {
                    val config = app.repository.reminderConfig.first()
                    if (config.enabled) {
                        AlarmScheduler.scheduleReminders(context, config)
                    }
                } catch (_: Exception) { }
            }
        }
    }
}
