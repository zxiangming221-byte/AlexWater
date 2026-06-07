package com.nous.waterwell.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nous.waterwell.MainActivity
import com.nous.waterwell.R

object NotificationHelper {
    const val CHANNEL_ID = "water_reminder"
    const val CHANNEL_NAME = "喝水提醒"
    const val NOTIFICATION_ID = 1001
    const val ACTION_DRINK_250 = "com.nous.waterwell.ACTION_DRINK_250"
    const val ACTION_DRINK_500 = "com.nous.waterwell.ACTION_DRINK_500"
    const val ACTION_SKIP = "com.nous.waterwell.ACTION_SKIP"
    const val EXTRA_MINUTES_SINCE = "minutes_since_last_drink"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context, minutesSinceLastDrink: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Create the intent for opening the app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Drink 250ml action via BroadcastReceiver
        val drink250Intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_DRINK_250
            putExtra(EXTRA_MINUTES_SINCE, minutesSinceLastDrink)
        }
        val drink250PendingIntent = PendingIntent.getBroadcast(
            context, 1, drink250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Drink 500ml action
        val drink500Intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_DRINK_500
            putExtra(EXTRA_MINUTES_SINCE, minutesSinceLastDrink)
        }
        val drink500PendingIntent = PendingIntent.getBroadcast(
            context, 2, drink500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Skip action
        val skipIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SKIP
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 3, skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (minutesSinceLastDrink > 0) {
            context.getString(R.string.notification_body, minutesSinceLastDrink)
        } else {
            "该补充水分了，喝一杯吧！每天${context.getString(R.string.app_name)}关心你的健康 💙"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "💧 250ml", drink250PendingIntent)
            .addAction(0, "💦 500ml", drink500PendingIntent)
            .addAction(0, "⏭ 跳过", skipPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
