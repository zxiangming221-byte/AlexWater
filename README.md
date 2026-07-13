<img src="https://img.shields.io/badge/platform-Android%208.0%2B-green" alt="Platform"> <img src="https://img.shields.io/badge/language-Kotlin-blueviolet" alt="Kotlin"> <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-blue" alt="Compose"> <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="License">

# AlexWater

喝水提醒 Android 应用。不搞花哨的东西 —— 提醒该响的时候一定响，记录该记的一笔不漏。

<p align="center">
  <a href="https://alexwater.pages.dev">项目主页</a>
</p>

---

## 核心功能

### 提醒系统
- 5 种提醒方案：每小时 / 1.5 小时 / 2 小时 / 自定义间隔 / 自定义时间点
- **日历集成**（主力）：直接写入系统日历，手机重启也按时响，通知方式在日历 App 里自己调
- **AlarmManager**（后备）：`setAlarmClock()` 最高优先级闹钟，日历不行就顶上
- 免打扰时段：晚上睡觉自动暂停，醒来继续
- 开机自启动重调度（BootReceiver）

### 饮水追踪
- 主页进度环 + 大数字显示，一目了然
- 三个可自定义的快捷杯量按钮，一键记录
- 今日记录时间线，轻点删除
- 达标自动弹庆祝横幅

### 数据统计
- 本周 / 上周切换，三格概览（日均 / 最佳 / 达标天数）
- 柱状图可视化每日摄入，目标线对照
- 趋势分析：7 日均值、波动范围、环比变化
- 时段分布：上午 / 下午 / 晚上喝水量占比

### 视觉设计
- 暗色 / 浅色主题，**逐色 250ms 过渡动画**（非 Crossfade 双重渲染）
- 进度环 spring 弹性动画 + 达标脉冲效果
- 柱状图入场交错动画（EaseOutCubic）
- 浮动水泡背景动画
- 水蓝色主题（#2196F3）贯穿全局

## 技术架构

```
Kotlin + Jetpack Compose + Material 3
├── DataStore Preferences 持久化
├── Navigation Compose 路由
├── ViewModel + StateFlow 状态管理
├── Canvas 自定义绘制 (进度环 / 柱状图)
├── Calendar Provider API (系统日历集成)
├── AlarmManager + BroadcastReceiver
└── JUnit 4 + MockK 单元测试
```

## 项目结构

```
app/src/main/java/com/alexwater/
├── AlexWaterApp.kt           # Application 入口
├── MainActivity.kt           # 唯一 Activity
├── alarm/
│   ├── AlarmScheduler.kt     # 闹钟调度
│   ├── AlarmReceiver.kt      # 闹钟接收
│   ├── AlarmAlertActivity.kt # 提醒弹窗
│   ├── CalendarReminderManager.kt  # 日历集成
│   ├── NotificationHelper.kt # 通知管理
│   ├── PermissionHelper.kt   # 权限检查
│   ├── WaterReminderService.kt
│   └── BootReceiver.kt       # 开机恢复
├── data/
│   └── AppRepository.kt      # DataStore + 内存记录
├── model/
│   └── Models.kt             # 数据模型
└── ui/
    ├── home/       # 主页 + 进度环
    ├── reminders/  # 提醒设置
    ├── stats/      # 统计图表
    ├── history/    # 历史记录
    ├── settings/   # 设置（目标/杯量/主题/关于）
    ├── permission/ # 权限引导页
    ├── theme/      # Material 3 主题 + 动画
    ├── components/ # 复用组件
    └── navigation/ # 导航
```

## 运行

```bash
# Android Studio 打开 → Sync Gradle → Run
```

| 项目 | 版本 |
|------|------|
| 最低 SDK | Android 8.0 (API 26) |
| 目标 SDK | Android 14 (API 34) |
| Kotlin | 1.9.20 |
| Compose BOM | 2024.01.00 |
| Gradle | 8.2.0 |
| JDK | 17 |
