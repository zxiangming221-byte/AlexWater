package com.alexwater

import com.alexwater.model.WaterRecord
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * TC09-TC12: 数据模型测试
 */
class DataModelTest {

    @Test
    fun `TC09 - WaterRecord creation`() {
        val now = System.currentTimeMillis()
        val record = WaterRecord(
            id = 1,
            timestamp = now,
            amountMl = 200,
            date = LocalDate.now()
        )
        assertEquals(1, record.id)
        assertEquals(now, record.timestamp)
        assertEquals(200, record.amountMl)
        assertEquals(LocalDate.now(), record.date)
    }

    @Test
    fun `TC10 - DaySummary computes correctly`() {
        val today = LocalDate.now()
        val records = listOf(
            WaterRecord(1, System.currentTimeMillis(), 200, today),
            WaterRecord(2, System.currentTimeMillis(), 300, today),
        )
        val summary = com.alexwater.model.DaySummary(today, 500, 2000, records)
        assertEquals(500, summary.totalMl)
        assertEquals(2000, summary.goalMl)
        assertEquals(2, summary.records.size)
    }

    @Test
    fun `TC11 - AppSettings default values`() {
        val settings = com.alexwater.model.AppSettings()
        assertEquals(2000, settings.dailyGoalMl)
        assertEquals(listOf(200, 300, 500), settings.cupSizes)
        assertEquals(com.alexwater.model.Theme.DARK, settings.theme)
    }

    @Test
    fun `TC12 - WeekStats empty data`() {
        val stats = com.alexwater.model.WeekStats(emptyList(), 0, 0, 0)
        assertEquals(0, stats.averageMl)
        assertEquals(0, stats.bestMl)
        assertEquals(0, stats.goalDaysCount)
        assertTrue(stats.days.isEmpty())
    }
}
