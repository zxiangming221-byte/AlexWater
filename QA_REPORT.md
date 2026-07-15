# AlexWater QA Report — Comprehensive Bug Audit

**Date:** 2026-07-15 | **Tester:** QA Subagent | **Files Analyzed:** 43 Kotlin + 30 XML

---

## SUMMARY

| Severity   | Count |
|------------|-------|
| CRITICAL   | 3     |
| HIGH       | 7     |
| MEDIUM     | 12    |
| LOW        | 5     |
| **TOTAL**  | **27** |

---

## CRITICAL BUGS (3)

### BUG #1 — Alarm cancellation broken: action string mismatch

**File:** `app/.../alarm/AlarmScheduler.kt`  
**Lines:** 35-44 (cancelAll) vs 49-54 (scheduleAlarm)  
**Severity:** CRITICAL

**Description:** `cancelAll()` uses action `"ALARM_TRIGGER_$i"` (where `i` is 0–47), while `scheduleAlarm()` uses action `"ALARM_TRIGGER_$requestCode"` (where `requestCode` is 1000 + index). PendingIntent matching depends on action string equality. Since the action strings differ (`"ALARM_TRIGGER_0"` ≠ `"ALARM_TRIGGER_1000"`), `FLAG_NO_CREATE` will never find the existing PendingIntent, and **no alarms are ever actually cancelled**. This means:

- Every schedule call accumulates more alarms indefinitely (up to AlarmManager's per-app limit)
- Changing reminder config creates duplicates
- Device performance degrades over time with stale alarms

**Fix:** Unify the action naming. Change `cancelAll` to use `"ALARM_TRIGGER_${ALARM_REQUEST_BASE + i}"`:
```kotlin
action = "ALARM_TRIGGER_${ALARM_REQUEST_BASE + i}"
```
Or simpler: use a constant action string for all alarms since `requestCode` already differentiates them.

---

### BUG #2 — Midnight race condition: timestamp and date from different days

**File:** `app/.../data/AppRepository.kt`  
**Lines:** 73-74  
**Severity:** CRITICAL

**Description:** `System.currentTimeMillis()` is called at line 73 for `timestamp`, then `LocalDate.now()` at line 74 for `date`. If the clock crosses midnight between these two calls, the record gets yesterday's timestamp but today's date (or vice versa), creating an inconsistent record.

**Fix:** Capture a single `now` point:
```kotlin
val now = LocalDateTime.now()
dao.insert(WaterRecordEntity(
    timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    amountMl = amountMl,
    date = now.toLocalDate().format(dateFmt)
))
```

---

### BUG #3 — `fallbackToDestructiveMigration()` wipes all user data on schema change

**File:** `app/.../data/db/AppDatabase.kt`  
**Line:** 23  
**Severity:** CRITICAL

**Description:** Any database schema change (e.g., adding a column, changing types) between app versions **destroy all user water records** with no warning. This is `version = 1` with `fallbackToDestructiveMigration()`, so the first migration will be catastrophic.

**Fix:** Implement proper migrations with `Migration` objects, or at minimum use `fallbackToDestructiveMigration()` only as a last resort with a migration path for v1→v2.

---

## HIGH-SEVERITY BUGS (7)

### BUG #4 — Today's data doesn't reset at midnight if Flow doesn't re-emit

**File:** `app/.../ui/home/HomeViewModel.kt`  
**Line:** 35 (`LocalDate.now()` inside Flow collector)  
**Severity:** HIGH

**Description:** `HomeViewModel.init` collects `repository.records` and filters by `LocalDate.now()`. Room's Flow re-emits on any INSERT/DELETE — but if the user leaves the app open past midnight and does NOT interact, **no emission occurs** and `todayMl` still shows yesterday's total. This persists until the user adds/deletes a record or the ViewModel is recreated.

**Fix:** Add a ticker/time-based mechanism. Either:
- Use `flow { while(true) { emit(LocalDate.now()); delay(60_000) } }` and combine with records
- Or schedule a recomposition trigger at midnight via a coroutine that calculates `delay until next midnight`

---

### BUG #5 — CalendarReminderManager does NOT filter by DND periods

**File:** `app/.../alarm/CalendarReminderManager.kt`  
**Lines:** 143-188 (`generateReminderTimes`)  
**Severity:** HIGH

**Description:** `AlarmScheduler.calculateNextAlarms()` correctly filters out DND times (line 100). But `CalendarReminderManager.generateReminderTimes()` generates calendar events for 08:00–22:00 **without any DND filtering**. Users who set DND will still receive calendar reminder notifications during their DND hours.

**Fix:** Add DND filtering to `generateReminderTimes()`:
```kotlin
return result.sortedBy { it }
    .filter { time -> !config.dndEnabled || !AlarmScheduler.isInDnd(time.toLocalTime(), config.dndPeriods) }
```

---

### BUG #6 — PermissionSetup forces calendar permission (declared optional in code)

**File:** `app/.../MainActivity.kt` line 58 & `app/.../ui/permission/PermissionSetupScreen.kt`  
**Severity:** HIGH

**Description:** Code comment at `MainActivity.kt:58` states `// 日历是可选的，不影响基本功能`. However, `PermissionSetupScreen` includes `calendarGranted` in `allGranted` (line 57), making it MANDATORY. Users who deny calendar cannot proceed past the permission screen. This contradicts the stated design intent.

**Fix:** Remove `calendarGranted` from the mandatory check:
```kotlin
val allGranted = alarmGranted && notifyGranted && batteryGranted
```
Optionally show calendar as a "recommended but optional" permission.

---

### BUG #7 — Battery optimization check blocks all users (even non-reminder usage)

**File:** `app/.../MainActivity.kt`  
**Line:** 56 (`allPermissionsGranted()`)  
**Severity:** HIGH

**Description:** `allPermissionsGranted()` returns `false` if battery optimization is NOT ignored. This blocks every user from using the app at all until they disable battery optimization — even users who just want manual tracking and don't care about reminders.

**Fix:** Either remove battery optimization from the mandatory check, or offer a "skip" button on the permission screen. Move battery optimization to an in-app settings prompt instead of the onboarding gate.

---

### BUG #8 — BootReceiver doesn't sync calendar events after reboot

**File:** `app/.../alarm/BootReceiver.kt`  
**Lines:** 22-24  
**Severity:** HIGH

**Description:** `BootReceiver` only calls `AlarmScheduler.scheduleReminders()`. Calendar events are NOT recreated after reboot. If the user relies on calendar-based reminders, they stop working after a device restart until the app is manually opened.

**Fix:** Add calendar sync to BootReceiver:
```kotlin
CalendarReminderManager.sync(context, config)
```

---

### BUG #9 — RemindersViewModel.updateCustomInterval doesn't switch to CUSTOM_INTERVAL plan

**File:** `app/.../ui/reminders/RemindersViewModel.kt`  
**Lines:** 33-35  
**Severity:** HIGH

**Description:** `updateCustomInterval(minutes)` only updates `customIntervalMinutes` but does NOT set `plan = ReminderPlan.CUSTOM_INTERVAL`. If the user slides the interval slider while the plan is HOURLY, the interval value changes but the plan stays HOURLY — the custom interval is silently ignored.

**Fix:** Either set the plan to `CUSTOM_INTERVAL` inside `updateCustomInterval`, or disable the slider UI when plan is not `CUSTOM_INTERVAL`.

---

### BUG #10 — Hardcoded fake trend percentage in StatsScreen

**File:** `app/.../ui/stats/StatsScreen.kt`  
**Line:** 103  
**Severity:** HIGH

**Description:** `TrendRow("7 日均值", "${stats.averageMl} ml", trend = "↑ 5%", trendUp = true)` — the trend arrow and percentage are hardcoded to `"↑ 5%"`. This presents fabricated data to the user regardless of actual week-over-week change. This is misleading and erodes trust.

**Fix:** Compute actual week-over-week change by comparing with previous week's average, or remove the trend indicator entirely until real computation is implemented.

---

## MEDIUM-SEVERITY BUGS (12)

### BUG #11 — ProgressRing minimum bar height for 0-value days

**File:** `app/.../ui/components/BarChart.kt`  
**Line:** 40: `coerceIn(0.02f, 1f)`  
**Severity:** MEDIUM

**Description:** Bars for days with 0ml intake still show a tiny 2%-height sliver due to the `0.02f` minimum clamp. For empty weeks, all 7 bars show misleading tiny bars. The minimum makes sense to ensure 0-height bars aren't invisible, but creates a false-positive visual.

**Fix:** Use `coerceIn(0f, 1f)` and add a `minHeight` modifier on the Box instead:
```kotlin
Box(modifier = Modifier.width(28.dp).then(
    if (value > 0) Modifier.fillMaxHeight(animatedHeight)
    else Modifier.height(0.dp)
))
```

---

### BUG #12 — Water record deletion has no confirmation dialog

**File:** `app/.../ui/home/HomeScreen.kt`  
**Lines:** 188-206  
**Severity:** MEDIUM

**Description:** Tapping a record item reveals the delete button. A second tap on "删除" immediately deletes the record with no confirmation. Accidental double-taps or mistaken taps can delete records.

**Fix:** Add a confirmation dialog or undo mechanism.

---

### BUG #13 — RecordReceiver uses an unmanaged CoroutineScope (can lose data)

**File:** `app/.../alarm/NotificationHelper.kt`  
**Lines:** 70, 76-83  
**Severity:** MEDIUM

**Description:** `RecordReceiver` creates a `CoroutineScope(SupervisorJob() + Dispatchers.IO)` that is never cancelled. If the BroadcastReceiver's hosting process is killed after `onReceive` returns but before the coroutine insert completes, the water record is lost. `goAsync()` should be used.

**Fix:** Use `goAsync()`:
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    val pendingResult = goAsync()
    scope.launch {
        try { app.repository.addRecord(amountMl) }
        finally { pendingResult.finish() }
    }
}
```

---

### BUG #14 — SegmentedControl hides when CUSTOM plan is active

**File:** `app/.../ui/reminders/RemindersScreen.kt`  
**Lines:** 134-156  
**Severity:** MEDIUM

**Description:** When `plan` is `CUSTOM_INTERVAL` or `CUSTOM_TIMES`, `planIndex` is `-1` and the segment control is hidden. The user must toggle OFF the custom toggle to get back to a preset plan. This is confusing — there's no visual indicator that the segment control is the way to switch back.

**Fix:** Keep the segment control visible. Either add "自定义" as a segment option, or show all 3 preset options with none selected (deselected state) when custom is active.

---

### BUG #15 — No validation on record amounts (negative values accepted)

**File:** `app/.../data/AppRepository.kt`  
**Line:** 71-75 (`addRecord`)  
**Severity:** MEDIUM

**Description:** `addRecord(amountMl: Int)` accepts any integer, including 0 and negative values. While the UI doesn't expose negative inputs, the public API does — `RecordReceiver` could forward a negative amount, and any future programmatic caller could inject one.

**Fix:** Add validation:
```kotlin
suspend fun addRecord(amountMl: Int) {
    require(amountMl > 0) { "Amount must be positive" }
    ...
}
```

---

### BUG #16 — Int overflow risk in monthly total calculation

**File:** `app/.../ui/history/HistoryViewModel.kt`  
**Line:** 43  
**Severity:** MEDIUM

**Description:** `val totalMl = days.sumOf { d -> d.totalMl }` sums Int values. While realistic values (e.g., 9999 × 31 = 309,969) don't overflow, there's no guard against edge cases in corrupted data.

**Fix:** Use `.sumOf { it.totalMl.toLong() }` and cast back if needed, or just document that Int overflow at ~2.1B ml is impractical.

---

### BUG #17 — No duplicate record prevention

**File:** `app/.../data/db/WaterRecordDao.kt` & `WaterRecordEntity.kt`  
**Severity:** MEDIUM

**Description:** No unique constraint or upsert logic. Rapid double-taps on quick-add buttons create duplicate records with nearly identical timestamps.

**Fix:** Either debounce in the UI (ignore clicks within 500ms) or add a compound unique index on `(date, timestamp, amountMl)`.

---

### BUG #18 — Calendar sync runs on every app start even if reminders disabled

**File:** `app/.../AlexWaterApp.kt`  
**Line:** 42  
**Severity:** MEDIUM

**Description:** `CalendarReminderManager.sync()` is called unconditionally on app start. When reminders are disabled, `sync()` deletes all events (line 49) then skips creation (line 50-52) — so it's a no-op delete. But this still wastes a content provider query and is logically wrong (syncing "nothing" when disabled).

**Fix:** Wrap in the same `if (config.enabled)` check as `AlarmScheduler`:
```kotlin
if (config.enabled) {
    AlarmScheduler.scheduleReminders(this@AlexWaterApp, config)
    CalendarReminderManager.sync(this@AlexWaterApp, config)
}
```

---

### BUG #19 — CalendarReminderManager hardcodes 08:00–22:00 window

**File:** `app/.../alarm/CalendarReminderManager.kt`  
**Lines:** 144-145  
**Severity:** MEDIUM

**Description:** The reminder generation window (`startHour = 8`, `endHour = 22`) is hardcoded. This doesn't match AlarmScheduler which generates for the full 24 hours. Users who want reminders at 07:00 or 23:00 via CUSTOM_TIMES won't get calendar events for those times.

**Fix:** Use 0–24 range, or make configurable, or at minimum document this limitation.

---

### BUG #20 — Goal achievement banner has no exit animation

**File:** `app/.../ui/home/HomeScreen.kt`  
**Lines:** 86-113  
**Severity:** MEDIUM

**Description:** `AnimatedVisibility` only specifies `enter` transitions. When the user drops below the goal (e.g., deletes a record), the banner disappears without any exit animation — it just vanishes.

**Fix:** Add `exit = slideOutVertically() + fadeOut()`.

---

### BUG #21 — Empty test method and placeholder assertions in tests

**File:** `app/.../ReminderConfigTest.kt`  
**Lines:** 20 (placeholder), 58-60 (empty body)  
**Severity:** MEDIUM

**Description:** 
- Line 20: `assertEquals(2000, 2000) // placeholder` — always passes, tests nothing.
- Lines 58-60: `fun TC08...() { }` — empty test body, tests nothing.

These give false confidence in test coverage.

**Fix:** Replace with real assertions or remove the tests.

---

### BUG #22 — No debounce on quick-add buttons

**File:** `app/.../ui/home/HomeScreen.kt`, `app/.../ui/components/QuickButton.kt`  
**Severity:** MEDIUM

**Description:** Quick-add buttons have no click debouncing. Rapid consecutive taps can insert multiple records in quick succession (e.g., 3 taps = 600ml recorded instead of 200ml). This is realistic with fast double-taps on capacitive screens.

**Fix:** Add debounce in `HomeViewModel.addWater()` or use `clickable` with `onClick` ignoring re-clicks within ~500ms.

---

## LOW-SEVERITY BUGS (5)

### BUG #23 — HistoryViewModel month doesn't auto-advance at month boundary

**File:** `app/.../ui/history/HistoryViewModel.kt`  
**Line:** 17 (`_currentMonth = MutableStateFlow(YearMonth.now())` initialized once)  
**Severity:** LOW

**Description:** `_currentMonth` is set to `YearMonth.now()` at ViewModel creation only. If the user has the app open when crossing a month boundary (midnight of last day of month), the history page won't auto-advance to the new month. This is low severity because it's an edge case (app open at midnight on month end).

---

### BUG #24 — StatsScreen "摄入波动" calculation is misleading

**File:** `app/.../ui/stats/StatsScreen.kt`  
**Line:** 106: `"±${((stats.bestMl - (stats.days.minOfOrNull { it.totalMl } ?: 0)) / 2)} ml"`  
**Severity:** LOW

**Description:** This formula calculates `(max - min) / 2`, which is half the range — not a proper statistical deviation measure. For a proper measure, use standard deviation or at minimum rename the label to "波动范围".

---

### BUG #25 — Time distribution counts records, not volume

**File:** `app/.../ui/stats/StatsScreen.kt`  
**Lines:** 124-136  
**Severity:** LOW

**Description:** The 时段分布 section counts the *number* of records in each time period, not the total volume (ml). One 500ml record at 07:00 counts the same as one 50ml sip. A volume-based distribution would be more informative.

---

### BUG #26 — AlexWaterApp launches foreground service unconditionally

**File:** `app/.../AlexWaterApp.kt`  
**Lines:** 49-56  
**Severity:** LOW

**Description:** `startForegroundService()` is called in `onCreate()` unconditionally. Even if reminders are disabled, the foreground service runs. This shows a persistent notification "AlexWater 运行中" that users who only do manual tracking can't dismiss.

---

### BUG #27 — WaterBubbles has pointerInput with no gesture handling (dead code)

**File:** `app/.../ui/components/WaterBubbles.kt`  
**Line:** 33: `.pointerInput(Unit) { /* pass through */ }`  
**Severity:** LOW

**Description:** The `pointerInput` modifier has an empty lambda body — it's intended to "pass through" touch events but does nothing. It could be removed entirely, or if touch pass-through is needed, it's already the default behavior without `pointerInput`.

---

## VERIFIED CORRECT IMPLEMENTATIONS

To balance the report, these areas were inspected and found correct:

- ✅ `WaterRecordDao` queries use correct SQL with COALESCE for null safety
- ✅ `AppDatabase` singleton pattern with double-checked locking is correct
- ✅ `AppRepository` DataStore reads/writes are properly typed with fallbacks
- ✅ `AlarmScheduler.isInDnd()` correctly handles cross-midnight periods (verified by test suite)
- ✅ Custom interval coercion (`coerceAtLeast(15)`) works correctly
- ✅ Malformed DataStore entries are safely parsed with `.mapNotNull { try/catch }`
- ✅ `HistoryViewModel.nextMonth()` correctly prevents navigating to future months
- ✅ `ProgressRing` uses `animateFloatAsState` with spring for smooth progress animation
- ✅ Theme switching animates color transitions properly

---

## RECOMMENDED FIX PRIORITY

1. **Immediate (ship-blocking):** BUG #1 (alarm cancellation), BUG #2 (midnight race), BUG #3 (data destruction)
2. **Before release:** BUG #4–#10 (midnight reset, DND in calendar, permission gate, battery opt gate, boot calendar, custom interval, fake trend)
3. **Next sprint:** BUG #11–#22 (medium severity items)
4. **Backlog:** BUG #23–#27 (low severity / polish)
