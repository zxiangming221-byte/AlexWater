package com.alexwater

import com.alexwater.data.db.WaterRecordEntity
import com.alexwater.model.WaterRecord
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Tests for Room entity construction, DAO query patterns, and date-string round-trips.
 *
 * These validate the logic patterns used in AppRepository and the DAO without
 * needing an actual Room database (which requires Robolectric for unit tests).
 */
class DatabaseLogicTest {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ─── WaterRecordEntity construction ───────────────────────────────────

    @Test
    fun `entity creation with auto-generated id defaults to 0`() {
        val entity = WaterRecordEntity(
            timestamp = System.currentTimeMillis(),
            amountMl = 200,
            date = "2024-06-15"
        )
        assertEquals(0, entity.id)
    }

    @Test
    fun `entity creation with explicit id`() {
        val entity = WaterRecordEntity(
            id = 42,
            timestamp = 1000L,
            amountMl = 500,
            date = "2024-01-01"
        )
        assertEquals(42, entity.id)
        assertEquals(1000L, entity.timestamp)
        assertEquals(500, entity.amountMl)
        assertEquals("2024-01-01", entity.date)
    }

    @Test
    fun `entity with zero ml`() {
        val entity = WaterRecordEntity(
            timestamp = System.currentTimeMillis(),
            amountMl = 0,
            date = "2024-06-15"
        )
        assertEquals(0, entity.amountMl)
    }

    @Test
    fun `entity with large amount ml`() {
        val entity = WaterRecordEntity(
            timestamp = System.currentTimeMillis(),
            amountMl = Int.MAX_VALUE,
            date = "2024-06-15"
        )
        assertEquals(Int.MAX_VALUE, entity.amountMl)
    }

    // ─── Entity to Model conversion ───────────────────────────────────────

    @Test
    fun `entity to model round-trip preserves all fields`() {
        val entity = WaterRecordEntity(
            id = 7,
            timestamp = 1718400000000L,
            amountMl = 350,
            date = "2024-06-15"
        )
        val model = WaterRecord(
            id = entity.id,
            timestamp = entity.timestamp,
            amountMl = entity.amountMl,
            date = LocalDate.parse(entity.date, dateFmt)
        )
        assertEquals(entity.id, model.id)
        assertEquals(entity.timestamp, model.timestamp)
        assertEquals(entity.amountMl, model.amountMl)
        assertEquals(LocalDate.of(2024, 6, 15), model.date)
    }

    @Test
    fun `model to entity conversion preserves fields`() {
        val model = WaterRecord(
            id = 3,
            timestamp = System.currentTimeMillis(),
            amountMl = 250,
            date = LocalDate.of(2024, 12, 25)
        )
        val entity = WaterRecordEntity(
            id = model.id,
            timestamp = model.timestamp,
            amountMl = model.amountMl,
            date = model.date.format(dateFmt)
        )
        assertEquals("2024-12-25", entity.date)
        assertEquals(250, entity.amountMl)
        assertEquals(3, entity.id)
    }

    // ─── Date string formatting ───────────────────────────────────────────

    @Test
    fun `date format yyyy-MM-dd produces correct string`() {
        val date = LocalDate.of(2024, 1, 5)
        assertEquals("2024-01-05", date.format(dateFmt))
    }

    @Test
    fun `date format handles month and day padding`() {
        assertEquals("2024-12-31", LocalDate.of(2024, 12, 31).format(dateFmt))
        assertEquals("2024-01-01", LocalDate.of(2024, 1, 1).format(dateFmt))
    }

    @Test
    fun `date parsing round-trip preserves the date`() {
        val original = LocalDate.of(2024, 7, 15)
        val formatted = original.format(dateFmt)
        val parsed = LocalDate.parse(formatted, dateFmt)
        assertEquals(original, parsed)
    }

    @Test
    fun `date format produces lexicographically sortable strings`() {
        val dates = listOf(
            LocalDate.of(2024, 3, 15),
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
        )
        val sorted = dates.map { it.format(dateFmt) }.sorted()
        assertEquals("2024-01-01", sorted[0])
        assertEquals("2024-03-15", sorted[1])
        assertEquals("2024-12-31", sorted[2])
    }

    @Test
    fun `date range queries - BETWEEN works with string dates`() {
        // The DAO uses: WHERE date BETWEEN :start AND :end
        // String comparison works because yyyy-MM-dd is lexicographically sortable
        val start = "2024-06-01"
        val end = "2024-06-07"

        val dates = listOf(
            "2024-05-31", "2024-06-01", "2024-06-03", "2024-06-07", "2024-06-08"
        )
        val inRange = dates.filter { it in start..end }

        assertEquals(3, inRange.size)
        assertTrue(inRange.contains("2024-06-01"))
        assertTrue(inRange.contains("2024-06-03"))
        assertTrue(inRange.contains("2024-06-07"))
        assertFalse(inRange.contains("2024-05-31"))
        assertFalse(inRange.contains("2024-06-08"))
    }

    @Test
    fun `date range across months works with string comparison`() {
        val start = "2024-01-28"
        val end = "2024-02-03"

        val dates = listOf(
            "2024-01-27", "2024-01-28", "2024-01-31",
            "2024-02-01", "2024-02-03", "2024-02-04"
        )
        val inRange = dates.filter { it in start..end }

        assertEquals(4, inRange.size)
        assertTrue(inRange.contains("2024-01-28"))
        assertTrue(inRange.contains("2024-02-03"))
    }

    @Test
    fun `date range across years works with string comparison`() {
        val start = "2023-12-29"
        val end = "2024-01-02"

        val dates = listOf(
            "2023-12-28", "2023-12-29", "2023-12-31",
            "2024-01-01", "2024-01-02", "2024-01-03"
        )
        val inRange = dates.filter { it in start..end }

        assertEquals(4, inRange.size)
    }

    // ─── SUM/aggregation logic patterns ───────────────────────────────────

    @Test
    fun `sum of amounts by date matches DAO getTotalByDate pattern`() {
        val records = listOf(
            WaterRecordEntity(1, 1000L, 200, "2024-06-15"),
            WaterRecordEntity(2, 2000L, 300, "2024-06-15"),
            WaterRecordEntity(3, 3000L, 150, "2024-06-16"),
        )

        val targetDate = "2024-06-15"
        val total = records
            .filter { it.date == targetDate }
            .sumOf { it.amountMl }

        assertEquals(500, total)
    }

    @Test
    fun `COALESCE SUM returns zero pattern when no records for date`() {
        // DAO: SELECT COALESCE(SUM(amountMl), 0) FROM water_records WHERE date = :date
        // When no records match, should return 0
        val records = listOf(
            WaterRecordEntity(1, 1000L, 200, "2024-06-15"),
        )
        val targetDate = "2024-06-16"
        val total = records
            .filter { it.date == targetDate }
            .sumOf { it.amountMl }

        assertEquals(0, total)
    }

    @Test
    fun `sum with single record returns that amount`() {
        val records = listOf(WaterRecordEntity(1, 1000L, 500, "2024-06-15"))
        val total = records.sumOf { it.amountMl }
        assertEquals(500, total)
    }

    @Test
    fun `sum with empty list returns zero`() {
        val records: List<WaterRecordEntity> = emptyList()
        val total = records.sumOf { it.amountMl }
        assertEquals(0, total)
    }

    // ─── Sorting by timestamp DESC ────────────────────────────────────────

    @Test
    fun `records sorted by timestamp descending`() {
        val records = listOf(
            WaterRecordEntity(1, 1000L, 100, "2024-06-15"),
            WaterRecordEntity(2, 3000L, 300, "2024-06-15"),
            WaterRecordEntity(3, 2000L, 200, "2024-06-15"),
        )
        val sorted = records.sortedByDescending { it.timestamp }
        assertEquals(3000L, sorted[0].timestamp)
        assertEquals(2000L, sorted[1].timestamp)
        assertEquals(1000L, sorted[2].timestamp)
    }

    @Test
    fun `records sorted by date ASC then timestamp ASC for date range query`() {
        // DAO: ORDER BY date ASC, timestamp ASC
        val records = listOf(
            WaterRecordEntity(1, 3000L, 100, "2024-06-16"),
            WaterRecordEntity(2, 1000L, 200, "2024-06-15"),
            WaterRecordEntity(3, 2000L, 300, "2024-06-15"),
        )
        val sorted = records.sortedWith(compareBy<WaterRecordEntity> { it.date }.thenBy { it.timestamp })
        assertEquals("2024-06-15", sorted[0].date)
        assertEquals(1000L, sorted[0].timestamp)
        assertEquals("2024-06-15", sorted[1].date)
        assertEquals(2000L, sorted[1].timestamp)
        assertEquals("2024-06-16", sorted[2].date)
        assertEquals(3000L, sorted[2].timestamp)
    }

    // ─── DaySummary computation ───────────────────────────────────────────

    @Test
    fun `DaySummary with records matching goal marks day as goal met`() {
        val date = LocalDate.now()
        val records = listOf(
            WaterRecord(1, 1000L, 1000, date),
            WaterRecord(2, 2000L, 1000, date),
        )
        val summary = com.alexwater.model.DaySummary(date, 2000, 2000, records)
        assertTrue(summary.totalMl >= summary.goalMl)
        assertEquals(2000, summary.totalMl)
    }

    @Test
    fun `DaySummary with no records has zero total`() {
        val summary = com.alexwater.model.DaySummary(LocalDate.now(), 0, 2000, emptyList())
        assertEquals(0, summary.totalMl)
        assertTrue(summary.totalMl < summary.goalMl)
    }

    // ─── WeekStats computation pattern ────────────────────────────────────

    @Test
    fun `WeekStats with all days meeting goal has correct count`() {
        val days = (0..6).map { i ->
            val date = LocalDate.now().minusDays(i.toLong())
            com.alexwater.model.DaySummary(
                date, 2000, 2000,
                listOf(WaterRecord(i.toLong(), 1000L, 2000, date))
            )
        }
        val stats = com.alexwater.model.WeekStats(days, 2000, 2000, 7)
        assertEquals(7, stats.goalDaysCount)
        assertEquals(2000, stats.averageMl)
        assertEquals(2000, stats.bestMl)
    }

    @Test
    fun `WeekStats with no days meeting goal has zero goal count`() {
        val days = (0..6).map { i ->
            val date = LocalDate.now().minusDays(i.toLong())
            com.alexwater.model.DaySummary(date, 500, 2000, emptyList())
        }
        val stats = com.alexwater.model.WeekStats(days, 500, 500, 0)
        assertEquals(0, stats.goalDaysCount)
    }

    @Test
    fun `WeekStats average is integer division floored`() {
        // 500 + 500 + 500 = 1500, 1500/3 = 500
        val days = listOf(
            createDaySummary(500, 2000),
            createDaySummary(500, 2000),
            createDaySummary(500, 2000),
        )
        val avg = (days.sumOf { it.totalMl }.toFloat() / days.size).toInt()
        assertEquals(500, avg)
    }

    private fun createDaySummary(totalMl: Int, goalMl: Int): com.alexwater.model.DaySummary {
        return com.alexwater.model.DaySummary(LocalDate.now(), totalMl, goalMl, emptyList())
    }
}
