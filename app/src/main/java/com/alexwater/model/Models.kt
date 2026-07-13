package com.alexwater.model

import java.time.LocalDate
import java.time.LocalTime

/** 饮水记录 */
data class WaterRecord(
    val id: Long = 0,
    val timestamp: Long,
    val amountMl: Int,
    val date: LocalDate,
)

/** 每日汇总 */
data class DaySummary(
    val date: LocalDate,
    val totalMl: Int,
    val goalMl: Int,
    val records: List<WaterRecord>,
)

/** 提醒方案 */
enum class ReminderPlan {
    HOURLY, EVERY_1_5H, EVERY_2H, CUSTOM_INTERVAL, CUSTOM_TIMES,
}

/** 免打扰时段 */
data class DndPeriod(
    val start: LocalTime,
    val end: LocalTime,
)

/** 提醒配置 */
data class ReminderConfig(
    val enabled: Boolean = true,
    val plan: ReminderPlan = ReminderPlan.HOURLY,
    val customIntervalMinutes: Int = 45,
    val customTimes: List<LocalTime> = listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)),
    val dndEnabled: Boolean = true,
    val dndPeriods: List<DndPeriod> = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))),
)

/** 主题 */
enum class Theme { DARK, LIGHT }

/** 应用设置 */
data class AppSettings(
    val dailyGoalMl: Int = 2000,
    val cupSizes: List<Int> = listOf(200, 300, 500),
    val theme: Theme = Theme.DARK,
)

/** 周统计 */
data class WeekStats(
    val days: List<DaySummary>,
    val averageMl: Int,
    val bestMl: Int,
    val goalDaysCount: Int,
)

/** 月统计 */
data class MonthStats(
    val days: List<DaySummary>,
    val averageMl: Int,
    val goalDaysCount: Int,
    val totalLiters: Float,
)
