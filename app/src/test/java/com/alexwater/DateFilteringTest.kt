package com.alexwater

import com.alexwater.model.WaterRecord
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for daily reset logic — filtering records by date.
 * Mirrors the pattern used in HomeViewModel.init:
 *   val today = LocalDate.now()
 *   val todayList = allRecords.filter { it.date == today }.sortedByDescending { it.timestamp }
 */
class DateFilteringTest {

    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)
    private val tomorrow = today.plusDays(1)

    @Test
    fun `filter by today - returns only today records`() {
        val records = listOf(
            WaterRecord(1, System.currentTimeMillis(), 200, today),
            WaterRecord(2, System.currentTimeMillis() - 1000, 300, today),
            WaterRecord(3, System.currentTimeMillis(), 150, yesterday),
            WaterRecord(4, System.currentTimeMillis(), 400, yesterday),
        )
        val todayRecords = records.filter { it.date == today }
        assertEquals(2, todayRecords.size)
        assertTrue(todayRecords.all { it.date == today })
    }

    @Test
    fun `filter by today when all records are from other days returns empty`() {
        val records = listOf(
            WaterRecord(1, System.currentTimeMillis(), 200, yesterday),
            WaterRecord(2, System.currentTimeMillis(), 300, yesterday),
        )
        val todayRecords = records.filter { it.date == today }
        assertTrue(todayRecords.isEmpty())
    }

    @Test
    fun `filter by today when empty list returns empty`() {
        val records: List<WaterRecord> = emptyList()
        val todayRecords = records.filter { it.date == today }
        assertTrue(todayRecords.isEmpty())
    }

    @Test
    fun `sum of today records amountMl matches expected`() {
        val records = listOf(
            WaterRecord(1, System.currentTimeMillis(), 200, today),
            WaterRecord(2, System.currentTimeMillis(), 300, today),
            WaterRecord(3, System.currentTimeMillis(), 150, yesterday),
        )
        val todayMl = records.filter { it.date == today }.sumOf { it.amountMl }
        assertEquals(500, todayMl)
    }

    @Test
    fun `sum of today when no today records returns zero`() {
        val records = listOf(
            WaterRecord(1, System.currentTimeMillis(), 200, yesterday),
            WaterRecord(2, System.currentTimeMillis(), 300, yesterday),
        )
        val todayMl = records.filter { it.date == today }.sumOf { it.amountMl }
        assertEquals(0, todayMl)
    }

    @Test
    fun `records sorted by timestamp descending after date filter`() {
        val now = System.currentTimeMillis()
        val records = listOf(
            WaterRecord(1, now - 2000, 100, today),
            WaterRecord(2, now, 200, today),
            WaterRecord(3, now - 1000, 300, today),
        )
        val sorted = records.filter { it.date == today }.sortedByDescending { it.timestamp }
        // Most recent first
        assertTrue(sorted[0].timestamp >= sorted[1].timestamp)
        assertTrue(sorted[1].timestamp >= sorted[2].timestamp)
        assertEquals(200, sorted[0].amountMl) // newest = 200ml
    }

    @Test
    fun `cross-day boundary - yesterday records excluded from today`() {
        // A record at 23:59 yesterday should NOT appear in today's filter
        val lateYesterday = yesterday.atStartOfDay().plusHours(23).plusMinutes(59)
            .toEpochSecond(java.time.ZoneOffset.UTC) * 1000

        val records = listOf(
            WaterRecord(1, lateYesterday, 500, yesterday),
            WaterRecord(2, System.currentTimeMillis(), 200, today),
        )
        val todayRecords = records.filter { it.date == today }
        assertEquals(1, todayRecords.size)
        assertEquals(200, todayRecords.sumOf { it.amountMl })
    }

    @Test
    fun `multiple days in list - only today matters`() {
        val records = (0..6).flatMap { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            listOf(
                WaterRecord(daysAgo * 2L, System.currentTimeMillis(), 100, date),
                WaterRecord(daysAgo * 2L + 1, System.currentTimeMillis(), 150, date),
            )
        }
        val todayRecords = records.filter { it.date == today }
        assertEquals(2, todayRecords.size)
        assertEquals(250, todayRecords.sumOf { it.amountMl })
    }

    @Test
    fun `LocalDate equality works correctly across different instances`() {
        val d1 = LocalDate.now()
        val d2 = LocalDate.of(d1.year, d1.month, d1.dayOfMonth)
        assertEquals(d1, d2)
        assertTrue(d1 == d2)
    }

    @Test
    fun `date comparison - isBefore and isAfter with consecutive days`() {
        assertTrue(yesterday.isBefore(today))
        assertTrue(today.isBefore(tomorrow))
        assertFalse(today.isBefore(today))
        assertFalse(today.isAfter(today))
    }
}
