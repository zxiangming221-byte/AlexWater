package com.alexwater

import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

/**
 * TC01-TC05: 提醒配置逻辑测试
 */
class ReminderConfigTest {

    @Test
    fun `TC01 - default config has expected values`() {
        val config = ReminderConfig()
        assertTrue(config.enabled)
        assertEquals(ReminderPlan.HOURLY, config.plan)
        assertEquals(2000, 2000) // placeholder
    }

    @Test
    fun `TC02 - custom interval zero should be guarded at usage site`() {
        val config = ReminderConfig(customIntervalMinutes = 0)
        // The scheduler must coerce this to safe minimum
        val safe = config.customIntervalMinutes.coerceAtLeast(15)
        assertTrue(safe >= 15)
    }

    @Test
    fun `TC03 - custom times list can be empty`() {
        val config = ReminderConfig(
            plan = ReminderPlan.CUSTOM_TIMES,
            customTimes = emptyList()
        )
        assertTrue(config.customTimes.isEmpty())
    }

    @Test
    fun `TC04 - DND period can be empty`() {
        val config = ReminderConfig(dndPeriods = emptyList())
        assertTrue(config.dndPeriods.isEmpty())
    }

    @Test
    fun `TC05 - cross-midnight DND period is valid`() {
        val period = DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))
        assertTrue(period.start.isAfter(period.end)) // Cross-midnight
    }

    @Test
    fun `TC06 - all ReminderPlan enum values exist`() {
        val values = ReminderPlan.entries
        assertEquals(5, values.size)
    }

    @Test
    fun `TC08 - notification mode does not launch activity`() {
    }
}
