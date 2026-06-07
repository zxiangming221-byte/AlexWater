package com.nous.waterwell.data

/**
 * In-app language options. Stored in DataStore, applied at runtime.
 */
enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    SIMPLIFIED_CHINESE("zh-CN", "简体中文"),
    TRADITIONAL_CHINESE("zh-TW", "繁體中文");

    companion object {
        fun fromCode(code: String) = entries.find { it.code == code } ?: ENGLISH
    }
}

/**
 * Centralized UI strings in three languages.
 * Every screen reads from LocalStrings to enable runtime language switching.
 */
data class AppStrings(
    val appName: String,
    val greetingLate: String, val greetingEarly: String, val greetingMorning: String,
    val greetingNoon: String, val greetingAfternoon: String, val greetingEvening: String,
    val drank: String, val notDrunkYet: String, val keepGoing: String,
    val todayRecords: String, val delete: String,
    val resetTitle: String, val resetMessage: String, val resetConfirm: String, val cancel: String,
    val cup1: String, val cup2: String, val cup500: String,
    val goalAchieved: String, val remindersTitle: String, val remindersCount: String,
    val scheduleTitle: String, val scheduleDesc: String, val scheduleActive: String,
    val settingsTitle: String,
    val dailyGoal: String, val dailyGoalLabel: String,
    val cupSize: String, val cupSizeLabel: String, val cupSizeDesc: String,
    val wakeSleep: String, val wakeLabel: String, val sleepLabel: String, val wakeSleepDesc: String,
    val notifications: String, val remindersSwitch: String,
    val vibrationSwitch: String, val soundSwitch: String,
    val appearance: String, val darkMode: String, val lightMode: String, val accentLabel: String,
    val about: String, val aboutTitle: String, val aboutVersion: String, val aboutMessage: String,
    val aboutTapQr: String, val aboutScanQr: String,
    val qrSave: String, val qrSaved: String, val qrClose: String,
    val languageLabel: String,
    val ml: String
) {
    companion object {
        val EN = AppStrings(
            appName = "AlexWater", greetingLate = "Late night", greetingEarly = "Good morning",
            greetingMorning = "Good morning", greetingNoon = "Good afternoon",
            greetingAfternoon = "Good afternoon", greetingEvening = "Good evening",
            drank = "Drank", notDrunkYet = "Haven't drunk today", keepGoing = "Keep going, stay hydrated 💪",
            todayRecords = "Today's Log", delete = "Delete",
            resetTitle = "Reset Today", resetMessage = "Clear all of today's drink records? This cannot be undone.",
            resetConfirm = "Reset", cancel = "Cancel",
            cup1 = "1 Cup", cup2 = "2 Cups", cup500 = "500ml",
            goalAchieved = "🎉 Goal Achieved!", remindersTitle = "Today's Reminders",
            remindersCount = "%d reminders", scheduleTitle = "Hydration Plans",
            scheduleDesc = "Choose a plan and we'll remind you on schedule",
            scheduleActive = "Active",
            settingsTitle = "Settings",
            dailyGoal = "Daily Goal", dailyGoalLabel = "Target",
            cupSize = "Cup Size", cupSizeLabel = "Cup", cupSizeDesc = "Size of one cup in quick-add",
            wakeSleep = "Schedule", wakeLabel = "Wake", sleepLabel = "Sleep",
            wakeSleepDesc = "Reminders only during waking hours",
            notifications = "Notifications", remindersSwitch = "Reminders",
            vibrationSwitch = "Vibration", soundSwitch = "Sound",
            appearance = "Appearance", darkMode = "Dark", lightMode = "Light", accentLabel = "Accent",
            about = "About", aboutTitle = "AlexWater", aboutVersion = "v1.0.0",
            aboutMessage = "Built with love by one developer.\nEvery detail crafted with care.\nIf this app helps you, consider buying me a coffee! ☕",
            aboutTapQr = "Tap image to enlarge", aboutScanQr = "Scan to support",
            qrSave = "Save to Gallery", qrSaved = "Saved to gallery 📸", qrClose = "Close",
            languageLabel = "Language",
            ml = "ml"
        )

        val ZH_CN = AppStrings(
            appName = "AlexWater", greetingLate = "夜深了", greetingEarly = "早上好",
            greetingMorning = "早上好", greetingNoon = "中午好",
            greetingAfternoon = "下午好", greetingEvening = "晚上好",
            drank = "已喝水", notDrunkYet = "今天还没喝水呢", keepGoing = "继续加油，保持水分充足 💪",
            todayRecords = "今日记录", delete = "删除",
            resetTitle = "重置今日记录", resetMessage = "确定要清除今天所有的喝水记录吗？此操作不可撤销。",
            resetConfirm = "确定重置", cancel = "取消",
            cup1 = "一杯", cup2 = "两杯", cup500 = "500ml",
            goalAchieved = "🎉 目标达成！", remindersTitle = "今日提醒",
            remindersCount = "共 %d 次提醒", scheduleTitle = "科学饮水方案",
            scheduleDesc = "选择适合你的饮水方案，我们会按方案定时提醒你",
            scheduleActive = "当前使用",
            settingsTitle = "设置",
            dailyGoal = "饮水目标", dailyGoalLabel = "每日目标",
            cupSize = "单杯水量", cupSizeLabel = "单杯水量", cupSizeDesc = "快速添加按钮中「一杯」的水量",
            wakeSleep = "作息时间", wakeLabel = "起床时间", sleepLabel = "睡觉时间",
            wakeSleepDesc = "仅在清醒时段发送提醒",
            notifications = "通知", remindersSwitch = "喝水提醒",
            vibrationSwitch = "振动", soundSwitch = "声音",
            appearance = "外观", darkMode = "深色模式", lightMode = "浅色模式", accentLabel = "强调色",
            about = "关于", aboutTitle = "AlexWater · 喝水提醒", aboutVersion = "v1.0.0",
            aboutMessage = "这个 App 由我一个人开发维护。\n从设计到代码，每一个细节都倾注了心血。\n如果它曾帮助到你，欢迎请我喝杯咖啡 ☕",
            aboutTapQr = "点击图片放大查看", aboutScanQr = "微信扫码即可支持",
            qrSave = "保存到相册", qrSaved = "已保存到相册 📸", qrClose = "关闭",
            languageLabel = "语言",
            ml = "ml"
        )

        val ZH_TW = AppStrings(
            appName = "AlexWater", greetingLate = "夜深了", greetingEarly = "早安",
            greetingMorning = "早安", greetingNoon = "午安",
            greetingAfternoon = "午安", greetingEvening = "晚安",
            drank = "已喝水", notDrunkYet = "今天還沒喝水呢", keepGoing = "繼續加油，保持水分充足 💪",
            todayRecords = "今日記錄", delete = "刪除",
            resetTitle = "重置今日記錄", resetMessage = "確定要清除今天所有的喝水記錄嗎？此操作無法復原。",
            resetConfirm = "確定重置", cancel = "取消",
            cup1 = "一杯", cup2 = "兩杯", cup500 = "500ml",
            goalAchieved = "🎉 目標達成！", remindersTitle = "今日提醒",
            remindersCount = "共 %d 次提醒", scheduleTitle = "科學飲水方案",
            scheduleDesc = "選擇適合你的飲水方案，我們會按方案定時提醒你",
            scheduleActive = "目前使用",
            settingsTitle = "設定",
            dailyGoal = "飲水目標", dailyGoalLabel = "每日目標",
            cupSize = "單杯水量", cupSizeLabel = "單杯水量", cupSizeDesc = "快速新增按鈕中「一杯」的水量",
            wakeSleep = "作息時間", wakeLabel = "起床時間", sleepLabel = "睡覺時間",
            wakeSleepDesc = "僅在清醒時段發送提醒",
            notifications = "通知", remindersSwitch = "喝水提醒",
            vibrationSwitch = "震動", soundSwitch = "聲音",
            appearance = "外觀", darkMode = "深色模式", lightMode = "淺色模式", accentLabel = "強調色",
            about = "關於", aboutTitle = "AlexWater · 喝水提醒", aboutVersion = "v1.0.0",
            aboutMessage = "這個 App 由我一個人開發維護。\n從設計到程式碼，每一個細節都傾注了心血。\n如果它曾幫助到你，歡迎請我喝杯咖啡 ☕",
            aboutTapQr = "點擊圖片放大檢視", aboutScanQr = "微信掃碼即可支持",
            qrSave = "儲存到相簿", qrSaved = "已儲存到相簿 📸", qrClose = "關閉",
            languageLabel = "語言",
            ml = "ml"
        )

        fun forLanguage(lang: AppLanguage) = when (lang) {
            AppLanguage.ENGLISH -> EN
            AppLanguage.SIMPLIFIED_CHINESE -> ZH_CN
            AppLanguage.TRADITIONAL_CHINESE -> ZH_TW
        }
    }
}
