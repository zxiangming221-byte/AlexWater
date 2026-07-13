package com.alexwater.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alexwater.AlexWaterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object NotificationHelper {
    const val CHANNEL_ID = "water_reminder"
    const val SERVICE_CHANNEL_ID = "water_service"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "喝水提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时提醒你喝水，保持水分充足"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun createServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "后台服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "喝水提醒后台运行状态"
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun createRecordPendingIntent(context: Context, amountMl: Int): PendingIntent {
        val intent = Intent(context, RecordReceiver::class.java).apply {
            action = "RECORD_WATER"
            putExtra("amount_ml", amountMl)
        }
        return PendingIntent.getBroadcast(
            context,
            amountMl, // unique request code per amount
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * 从通知快捷按钮接收记录请求。
 * 通过 WaterRepository 将饮水量持久化到数据库。
 */
class RecordReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val amountMl = intent.getIntExtra("amount_ml", 200)
        val app = context.applicationContext as? AlexWaterApp ?: return

        scope.launch {
            try {
                app.repository.addRecord(amountMl)
                android.util.Log.i("RecordReceiver", "Recorded ${amountMl}ml via notification action")
            } catch (e: Exception) {
                android.util.Log.e("RecordReceiver", "Failed to record water", e)
            }
        }
    }
}
