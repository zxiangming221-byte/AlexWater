package com.alexwater.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    private const val ALARM_REQUEST_BASE = 1000
    private const val MAX_ALARMS = 48

    fun scheduleReminders(context: Context, config: ReminderConfig) {
        cancelAll(context)
        if (!config.enabled) return
        Log.d(TAG, "Scheduling reminders: plan=${config.plan}")
        val alarmTimes = calculateNextAlarms(config)
        alarmTimes.forEachIndexed { index, timeInMillis ->
            scheduleAlarm(context, timeInMillis, ALARM_REQUEST_BASE + index)
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until MAX_ALARMS) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "ALARM_TRIGGER_${ALARM_REQUEST_BASE + i}"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_REQUEST_BASE + i, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it); it.cancel() }
        }
    }

    private fun scheduleAlarm(context: Context, timeInMillis: Long, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ALARM_TRIGGER_$requestCode"
            putExtra("alarm_id", requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent), pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    /** 获取下一个整点时间 */
    private fun nextRoundedHour(now: LocalDateTime): LocalDateTime {
        return now.withMinute(0).withSecond(0).withNano(0).plusHours(1)
    }

    @VisibleForTesting
    internal fun calculateNextAlarms(config: ReminderConfig): List<Long> {
        val now = LocalDateTime.now()
        val zoneId = ZoneId.systemDefault()
        val startTime = nextRoundedHour(now)

        val rawTimes = when (config.plan) {
            ReminderPlan.HOURLY ->
                (0 until 24).map { startTime.plusHours(it.toLong()) }

            ReminderPlan.EVERY_1_5H ->
                (0 until 16).map { startTime.plusMinutes((it * 90).toLong()) }

            ReminderPlan.EVERY_2H ->
                (0 until 12).map { startTime.plusHours((it * 2).toLong()) }

            ReminderPlan.CUSTOM_INTERVAL -> {
                val safeInterval = config.customIntervalMinutes.coerceAtLeast(15)
                val count = (24 * 60) / safeInterval
                (0 until count).map { now.plusMinutes((it * safeInterval.toLong()).toLong()) }
            }

            ReminderPlan.CUSTOM_TIMES ->
                config.customTimes.map { time ->
                    val scheduled = LocalDateTime.of(LocalDate.now(), time)
                    if (scheduled.isBefore(now)) scheduled.plusDays(1) else scheduled
                }
        }

        return rawTimes
            .filter { it.isAfter(now) }
            .filter { time -> !config.dndEnabled || !isInDnd(time.toLocalTime(), config.dndPeriods) }
            .take(MAX_ALARMS)
            .map { it.atZone(zoneId).toInstant().toEpochMilli() }
    }

    @VisibleForTesting
    internal fun isInDnd(time: LocalTime, dndPeriods: List<DndPeriod>): Boolean {
        return dndPeriods.any { period ->
            if (period.start.isBefore(period.end)) {
                // 正常时段: start ≤ time < end
                !time.isBefore(period.start) && time.isBefore(period.end)
            } else {
                // 跨午夜 (如 22:00 - 08:00): time ≥ start OR time < end
                !time.isBefore(period.start) || time.isBefore(period.end)
            }
        }
    }
}
