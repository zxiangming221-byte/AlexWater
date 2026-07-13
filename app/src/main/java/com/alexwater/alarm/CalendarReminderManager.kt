package com.alexwater.alarm

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.TimeZone

/**
 * 日历集成 — 系统级日历提醒。
 * 
 * 为什么比 AlarmManager 更可靠：
 * 1. 日历事件是系统级数据，不受 APP 进程状态影响
 * 2. 日历提醒在 Doze/Standby 模式下正常触发
 * 3. 厂商 ROM 不会拦截系统日历广播
 * 4. 即使 APP 被杀/强制停止，日历提醒依然生效
 *
 * 用法：在 ReminderConfig 变更时调用 sync()
 */
object CalendarReminderManager {
    private const val TAG = "CalendarReminder"
    private const val EVENT_TITLE = "💧 AlexWater 喝水提醒"
    private const val EVENT_DESC = "该喝水了！打开 AlexWater 记录饮水"
    private const val CALENDAR_ACCOUNT_NAME = "AlexWater"

    /** 检查是否有日历权限 */
    fun hasPermission(context: Context): Boolean {
        val read = context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
        val write = context.checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR)
        return read == android.content.pm.PackageManager.PERMISSION_GRANTED &&
               write == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /** 同步日历事件：删除旧的，创建新的 */
    fun sync(context: Context, config: ReminderConfig) {
        if (!hasPermission(context)) {
            Log.d(TAG, "No calendar permission, skipping sync")
            return
        }
        try {
            deleteAllEvents(context)
            if (config.enabled) {
                createEvents(context, config)
            }
            Log.d(TAG, "Calendar sync complete: enabled=${config.enabled}")
        } catch (e: Exception) {
            Log.e(TAG, "Calendar sync failed", e)
        }
    }

    /** 获取默认日历 ID */
    private fun getDefaultCalendarId(context: Context): Long {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        // Fallback: create our own local calendar
        return createLocalCalendar(context)
    }

    private fun createLocalCalendar(context: Context): Long {
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, CALENDAR_ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "AlexWater 提醒")
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0x2196F3)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 0)
        }
        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        val newUri = context.contentResolver.insert(uri, values)
        return ContentUris.parseId(newUri ?: return -1)
    }

    /** 创建未来 3 天的提醒事件 */
    private fun createEvents(context: Context, config: ReminderConfig) {
        val calId = getDefaultCalendarId(context)
        if (calId < 0) return

        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now()

        val times = generateReminderTimes(config, now)
        for (time in times) {
            if (time.isBefore(now)) continue
            val startMillis = time.atZone(zoneId).toInstant().toEpochMilli()
            val endMillis = startMillis + 5 * 60 * 1000 // 5 minutes duration

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.TITLE, EVENT_TITLE)
                put(CalendarContract.Events.DESCRIPTION, EVENT_DESC)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, zoneId.id)
                put(CalendarContract.Events.HAS_ALARM, 1)
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
            }
            val eventUri = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI, values
            )
            eventUri?.let { addReminder(context, it, startMillis) }
        }
    }

    /** 为日历事件添加提醒（提前 0 分钟，准时提醒） */
    private fun addReminder(context: Context, eventUri: Uri, eventStartMillis: Long) {
        val eventId = ContentUris.parseId(eventUri)
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, 0)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
    }

    /** 删除所有 AlexWater 日历事件 */
    private fun deleteAllEvents(context: Context) {
        val selection = "${CalendarContract.Events.TITLE} = ?"
        val args = arrayOf(EVENT_TITLE)
        context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI, selection, args
        )
    }

    /** 根据提醒配置生成提醒时间列表 */
    private fun generateReminderTimes(config: ReminderConfig, now: LocalDateTime): List<LocalDateTime> {
        val startHour = 8 // 从早上 8 点开始
        val endHour = 22  // 到晚上 10 点结束
        val days = 3      // 生成 3 天的提醒

        val result = mutableListOf<LocalDateTime>()

        for (day in 0 until days) {
            val baseDate = now.toLocalDate().plusDays(day.toLong())

            when (config.plan) {
                ReminderPlan.HOURLY -> {
                    for (h in startHour until endHour) {
                        result.add(LocalDateTime.of(baseDate, LocalTime.of(h, 0)))
                    }
                }
                ReminderPlan.EVERY_1_5H -> {
                    var t = LocalTime.of(startHour, 0)
                    while (t.isBefore(LocalTime.of(endHour, 0))) {
                        result.add(LocalDateTime.of(baseDate, t))
                        t = t.plusMinutes(90)
                    }
                }
                ReminderPlan.EVERY_2H -> {
                    for (h in startHour until endHour step 2) {
                        result.add(LocalDateTime.of(baseDate, LocalTime.of(h, 0)))
                    }
                }
                ReminderPlan.CUSTOM_INTERVAL -> {
                    val safeInterval = config.customIntervalMinutes.coerceAtLeast(15)
                    var t = LocalTime.of(startHour, 0)
                    while (t.isBefore(LocalTime.of(endHour, 0))) {
                        result.add(LocalDateTime.of(baseDate, t))
                        t = t.plusMinutes(safeInterval.toLong())
                    }
                }
                ReminderPlan.CUSTOM_TIMES -> {
                    config.customTimes.forEach { time ->
                        result.add(LocalDateTime.of(baseDate, time))
                    }
                }
            }
        }

        return result.sortedBy { it }
    }
}
