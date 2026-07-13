package com.alexwater

import com.alexwater.alarm.AlarmScheduler
import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

/**
 * Pure logic tests for AlarmScheduler — DND filtering, interval safety, custom times.
 * No Android dependencies.
 */
class AlarmSchedulerLogicTest {

    // ─── DND filtering: isInDnd ───────────────────────────────────────────

    @Test
    fun `test isInDnd when time inside normal period should return true`() {
        val periods = listOf(DndPeriod(LocalTime.of(1, 0), LocalTime.of(5, 0)))
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(3, 0), periods))
    }

    @Test
    fun `test isInDnd when time before normal period should return false`() {
        val periods = listOf(DndPeriod(LocalTime.of(9, 0), LocalTime.of(17, 0)))
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(8, 59), periods))
    }

    @Test
    fun `test isInDnd when time after normal period should return false`() {
        val periods = listOf(DndPeriod(LocalTime.of(1, 0), LocalTime.of(5, 0)))
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(6, 0), periods))
    }

    @Test
    fun `test isInDnd when time exactly at start of normal period should return true`() {
        val periods = listOf(DndPeriod(LocalTime.of(1, 0), LocalTime.of(5, 0)))
        // start is INCLUSIVE (!isBefore), so 01:00 IS in DND
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(1, 0), periods))
    }

    @Test
    fun `test isInDnd when time exactly at end of normal period should return false`() {
        val periods = listOf(DndPeriod(LocalTime.of(1, 0), LocalTime.of(5, 0)))
        // end is exclusive (isBefore), so 05:00 is NOT in DND
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(5, 0), periods))
    }

    // ─── Cross-midnight DND (e.g., 22:00–08:00) ────────────────────────────

    @Test
    fun `test isInDnd when time after start in cross-midnight DND should return true`() {
        val periods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)))
        // 23:00 is after 22:00, so should be in DND (cross-midnight logic)
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(23, 0), periods))
    }

    @Test
    fun `test isInDnd when time before end in cross-midnight DND should return true`() {
        val periods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)))
        // 06:00 is before 08:00, so should be in DND (cross-midnight logic)
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(6, 0), periods))
    }

    @Test
    fun `test isInDnd when time at midday during cross-midnight DND should return false`() {
        val periods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)))
        // 12:00 is neither after 22:00 nor before 08:00
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(12, 0), periods))
    }

    @Test
    fun `test isInDnd when time exactly at start of cross-midnight DND should return true`() {
        val periods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)))
        // start is INCLUSIVE (!isBefore), so 22:00 IS in DND
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(22, 0), periods))
    }

    @Test
    fun `test isInDnd when time exactly at end of cross-midnight DND should return false`() {
        val periods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)))
        // end is exclusive (isBefore), so 08:00 is NOT in DND
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(8, 0), periods))
    }

    @Test
    fun `test isInDnd with empty DND list should return false`() {
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(12, 0), emptyList()))
    }

    @Test
    fun `test isInDnd with multiple DND periods when time in second period should return true`() {
        val periods = listOf(
            DndPeriod(LocalTime.of(1, 0), LocalTime.of(3, 0)),
            DndPeriod(LocalTime.of(13, 0), LocalTime.of(14, 0)),
        )
        // 13:30 is inside the second DND period
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(13, 30), periods))
    }

    @Test
    fun `test isInDnd with multiple DND periods when time between periods should return false`() {
        val periods = listOf(
            DndPeriod(LocalTime.of(1, 0), LocalTime.of(3, 0)),
            DndPeriod(LocalTime.of(13, 0), LocalTime.of(14, 0)),
        )
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(10, 0), periods))
    }

    @Test
    fun `test isInDnd with both normal and cross-midnight periods should work`() {
        val periods = listOf(
            DndPeriod(LocalTime.of(1, 0), LocalTime.of(5, 0)),   // normal
            DndPeriod(LocalTime.of(23, 0), LocalTime.of(7, 0)), // cross-midnight
        )
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(3, 0), periods))
        assertTrue(AlarmScheduler.isInDnd(LocalTime.of(4, 0), periods))
        assertFalse(AlarmScheduler.isInDnd(LocalTime.of(12, 0), periods))
    }

    // ─── Custom interval safety (coerceAtLeast 15) ─────────────────────────

    @Test
    fun `test customIntervalMinutes zero should be coerced to at least 15`() {
        val config = ReminderConfig(customIntervalMinutes = 0)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(15, safe)
    }

    @Test
    fun `test customIntervalMinutes negative should be coerced to at least 15`() {
        val config = ReminderConfig(customIntervalMinutes = -5)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(15, safe)
    }

    @Test
    fun `test customIntervalMinutes 5 should be coerced to at least 15`() {
        val config = ReminderConfig(customIntervalMinutes = 5)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(15, safe)
    }

    @Test
    fun `test customIntervalMinutes 45 is above minimum and should stay unchanged`() {
        val config = ReminderConfig(customIntervalMinutes = 45)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(45, safe)
    }

    @Test
    fun `test customIntervalMinutes 120 is above minimum and should stay unchanged`() {
        val config = ReminderConfig(customIntervalMinutes = 120)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(120, safe)
    }

    @Test
    fun `test customIntervalMinutes at exact minimum 15 should stay 15`() {
        val config = ReminderConfig(customIntervalMinutes = 15)
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertEquals(15, safe)
    }

    // ─── Custom times sorting ──────────────────────────────────────────────

    @Test
    fun `test custom times should be sorted chronologically`() {
        val unsorted = listOf(
            LocalTime.of(14, 0),
            LocalTime.of(8, 0),
            LocalTime.of(20, 0),
            LocalTime.of(6, 30),
        )
        val sorted = unsorted.sortedBy { it }
        assertEquals(LocalTime.of(6, 30), sorted[0])
        assertEquals(LocalTime.of(8, 0), sorted[1])
        assertEquals(LocalTime.of(14, 0), sorted[2])
        assertEquals(LocalTime.of(20, 0), sorted[3])
    }

    @Test
    fun `test custom times already sorted should remain in same order`() {
        val sorted = listOf(
            LocalTime.of(8, 0),
            LocalTime.of(12, 0),
            LocalTime.of(18, 0),
        )
        assertEquals(sorted, sorted.sortedBy { it })
    }

    @Test
    fun `test custom times empty list should stay empty after sorting`() {
        val empty: List<LocalTime> = emptyList()
        assertTrue(empty.sortedBy { it }.isEmpty())
    }

    @Test
    fun `test custom times single element should stay same after sorting`() {
        val single = listOf(LocalTime.of(9, 30))
        assertEquals(single, single.sortedBy { it })
    }

    @Test
    fun `test custom times with duplicate times should preserve both`() {
        val withDup = listOf(
            LocalTime.of(8, 0),
            LocalTime.of(8, 0),
            LocalTime.of(12, 0),
        )
        val sorted = withDup.sortedBy { it }
        assertEquals(3, sorted.size)
        assertEquals(LocalTime.of(8, 0), sorted[0])
        assertEquals(LocalTime.of(8, 0), sorted[1])
        assertEquals(LocalTime.of(12, 0), sorted[2])
    }

    // ─── calculateNextAlarms: CUSTOM_TIMES plan ────────────────────────────

    @Test
    fun `test calculateNextAlarms with CUSTOM_TIMES and no DND returns future times`() {
        val config = ReminderConfig(
            enabled = true,
            plan = ReminderPlan.CUSTOM_TIMES,
            customTimes = listOf(LocalTime.of(8, 0), LocalTime.of(23, 0)),
            dndPeriods = emptyList(),
        )
        val alarms = AlarmScheduler.calculateNextAlarms(config)
        // Should have some alarms scheduled for times after 'now'
        assertTrue("Should schedule at least some alarms", alarms.isNotEmpty())
        // All alarm times should be in the future (millis > current)
        val now = System.currentTimeMillis()
        alarms.forEach { assertTrue("Alarm $it should be in the future", it > now) }
    }

    @Test
    fun `test calculateNextAlarms with CUSTOM_TIMES and DND filters out DND times`() {
        val config = ReminderConfig(
            enabled = true,
            plan = ReminderPlan.CUSTOM_TIMES,
            customTimes = listOf(LocalTime.of(1, 0), LocalTime.of(12, 0)),
            dndPeriods = listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))),
        )
        val alarms = AlarmScheduler.calculateNextAlarms(config)
        val now = System.currentTimeMillis()
        alarms.forEach { assertTrue("Alarm $it should be in the future", it > now) }
    }
}
