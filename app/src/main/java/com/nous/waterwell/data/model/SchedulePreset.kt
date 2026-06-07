package com.nous.waterwell.data.model

/**
 * Pre-built scientific hydration schedules.
 * Not a Room entity — these are hardcoded presets shown to the user.
 */
data class SchedulePreset(
    val id: String,
    val name: String,
    val description: String,
    val totalMl: Int,
    val times: List<ReminderTime>
)

data class ReminderTime(
    val hour: Int,
    val minute: Int,
    val suggestedMl: Int = 250
)

object SchedulePresets {
    val ALL = listOf(
        SchedulePreset(
            id = "standard_8",
            name = "标准8杯法",
            description = "每天8杯水，每杯250ml，均匀分布在醒着的时间，总计2000ml。这是最经典、最被广泛推荐的饮水方案。",
            totalMl = 2000,
            times = listOf(
                ReminderTime(7, 0, 250),
                ReminderTime(9, 0, 250),
                ReminderTime(11, 0, 250),
                ReminderTime(13, 0, 250),
                ReminderTime(15, 0, 250),
                ReminderTime(17, 0, 250),
                ReminderTime(19, 0, 250),
                ReminderTime(21, 0, 250)
            )
        ),
        SchedulePreset(
            id = "office",
            name = "办公族方案",
            description = "专为上班族设计，集中在9:00-18:00办公时段，每90分钟提醒一次，配合工作节奏，总计1800ml。",
            totalMl = 1800,
            times = listOf(
                ReminderTime(8, 0, 300),
                ReminderTime(9, 30, 200),
                ReminderTime(11, 0, 250),
                ReminderTime(13, 30, 200),
                ReminderTime(15, 0, 250),
                ReminderTime(16, 30, 200),
                ReminderTime(18, 0, 200),
                ReminderTime(20, 0, 200)
            )
        ),
        SchedulePreset(
            id = "small_frequent",
            name = "少量多次法",
            description = "每隔1小时喝一小杯(150ml)，让身体持续保持水分平衡，避免一次性大量饮水带来的不适。总计1800ml。",
            totalMl = 1800,
            times = listOf(
                ReminderTime(7, 0, 150),
                ReminderTime(8, 0, 150),
                ReminderTime(9, 0, 150),
                ReminderTime(10, 0, 150),
                ReminderTime(11, 0, 150),
                ReminderTime(12, 0, 150),
                ReminderTime(14, 0, 150),
                ReminderTime(15, 0, 150),
                ReminderTime(16, 0, 150),
                ReminderTime(17, 0, 150),
                ReminderTime(19, 0, 150),
                ReminderTime(21, 0, 150)
            )
        ),
        SchedulePreset(
            id = "sports",
            name = "运动爱好者",
            description = "照顾运动人群的补水需求，运动前后加量，总计2500ml。适合每天运动30分钟以上的人群。",
            totalMl = 2500,
            times = listOf(
                ReminderTime(7, 0, 350),
                ReminderTime(9, 0, 250),
                ReminderTime(11, 0, 200),
                ReminderTime(13, 0, 250),
                ReminderTime(15, 0, 200),
                ReminderTime(17, 0, 350),
                ReminderTime(18, 30, 300),
                ReminderTime(20, 0, 300),
                ReminderTime(21, 30, 300)
            )
        ),
        SchedulePreset(
            id = "morning_focus",
            name = "晨间唤醒法",
            description = "强调早晨补水，起床后前3小时完成全天40%的饮水，帮助身体快速启动代谢。总计2000ml。",
            totalMl = 2000,
            times = listOf(
                ReminderTime(6, 30, 400),
                ReminderTime(8, 0, 300),
                ReminderTime(9, 30, 200),
                ReminderTime(12, 0, 250),
                ReminderTime(14, 30, 200),
                ReminderTime(16, 0, 200),
                ReminderTime(18, 0, 200),
                ReminderTime(20, 0, 250)
            )
        )
    )
}
