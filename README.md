# 💧 AlexWater — 喝水提醒

> 科学饮水，健康生活 · Built with Kotlin & Jetpack Compose

<p align="center">
  <img src="https://img.shields.io/badge/version-1.3.0-blue" alt="version">
  <img src="https://img.shields.io/badge/platform-Android%208.0%2B-green" alt="platform">
  <img src="https://img.shields.io/badge/language-Kotlin-purple" alt="language">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
</p>

## 📱 介绍

AlexWater 是一款专注于科学饮水的 Android 应用。它提供 5 种科学饮水方案，支持后台定时提醒、自定义目标和深色模式，帮助你养成健康的饮水习惯。

### ✨ 核心功能

- 🧪 **5 种科学方案** — 标准 / 减肥 / 运动 / 夏季 / 自定义
- ⏰ **后台定时提醒** — WorkManager 保活，不杀进程
- 📊 **饮水记录** — 每日进度环 + 历史统计
- 🎨 **8 种强调色 + 深色模式** — iOS 风格毛玻璃界面
- 🌐 **三语支持** — 中文 / English / 繁體中文
- 🔄 **一键重置** — 每日记录快速清零

### 🛠 技术栈

| 模块 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 数据持久化 | Room Database |
| 后台任务 | WorkManager |
| 设置存储 | DataStore Preferences |
| 构建 | Gradle KTS + R8 |

## 📥 下载

👉 [最新版 APK](https://github.com/zxiangming221-byte/AlexWater/releases/latest)

## 🚀 构建

```bash
# 克隆仓库
git clone https://github.com/zxiangming221-byte/AlexWater.git
cd AlexWater

# 用 Android Studio 打开，或命令行构建
./gradlew assembleRelease
```

## 📄 许可

MIT License © 2025 Alex
