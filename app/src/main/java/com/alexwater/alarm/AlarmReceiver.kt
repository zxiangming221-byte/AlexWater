package com.alexwater.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alexwater.MainActivity

/**
 * 闹钟广播接收器。
 * 日历提醒已是主力，AlarmManager 作为后备。
 * 收到广播后发送通知栏提醒，用户点击进入 APP。
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannel(context)
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 该喝水了")
            .setContentText("你已经一段时间没喝水了，补充一下水分吧")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(0, "+200 ml", NotificationHelper.createRecordPendingIntent(context, 200))
            .addAction(0, "+300 ml", NotificationHelper.createRecordPendingIntent(context, 300))
            .addAction(0, "+500 ml", NotificationHelper.createRecordPendingIntent(context, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) { }
    }
}
