package com.alexwater

import com.alexwater.data.AppRepository
import com.alexwater.model.WaterRecord
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for AppRepository data operations (add, delete, filter by date, ID generation).
 * Uses MockK to mock Context – only in-memory record operations are tested (no DataStore I/O).
 */
class AppRepositoryTest {

    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        repository = AppRepository(mockContext)
    }

    // ─── addRecord ─────────────────────────────────────────────────────────

    @Test
    fun `test addRecord should return a WaterRecord with auto-generated ID`() {
        val record = repository.addRecord(200)
        assertNotNull(record)
        assertTrue("ID should be > 0", record.id > 0)
        assertEquals(200, record.amountMl)
        assertEquals(LocalDate.now(), record.date)
        assertTrue("Timestamp should be recent", record.timestamp > 0)
    }

    @Test
    fun `test addRecord should increment ID for each new record`() {
        val r1 = repository.addRecord(200)
        val r2 = repository.addRecord(300)
        val r3 = repository.addRecord(500)

        assertEquals(1, r1.id)
        assertEquals(2, r2.id)
        assertEquals(3, r3.id)
    }

    @Test
    fun `test addRecord should increase record count`() {
        assertEquals(0, repository.getAllRecords().size)

        repository.addRecord(200)
        assertEquals(1, repository.getAllRecords().size)

        repository.addRecord(300)
        assertEquals(2, repository.getAllRecords().size)
    }

    // ─── deleteRecord ──────────────────────────────────────────────────────

    @Test
    fun `test deleteRecord should remove the specified record`() {
        val r1 = repository.addRecord(200)
        val r2 = repository.addRecord(300)

        repository.deleteRecord(r1)
        val remaining = repository.getAllRecords()
        assertEquals(1, remaining.size)
        assertEquals(r2.id, remaining[0].id)
    }

    @Test
    fun `test deleteRecord with non-existent record should not crash`() {
        repository.addRecord(200)
        val fakeRecord = WaterRecord(id = 999, timestamp = 0, amountMl = 100, date = LocalDate.now())

        repository.deleteRecord(fakeRecord)
        assertEquals(1, repository.getAllRecords().size)
    }

    @Test
    fun `test deleteRecord should allow deleting last record`() {
        val r1 = repository.addRecord(200)
        repository.deleteRecord(r1)
        assertTrue(repository.getAllRecords().isEmpty())
    }

    @Test
    fun `test deleteRecord multiple times should handle empty state gracefully`() {
        val r1 = repository.addRecord(200)
        repository.deleteRecord(r1)
        repository.deleteRecord(r1) // double delete
        assertTrue(repository.getAllRecords().isEmpty())
    }

    // ─── getTodayRecords ────────────────────────────────────────────────────

    @Test
    fun `test getTodayRecords should return only records from today`() {
        repository.addRecord(200) // uses LocalDate.now()
        repository.addRecord(300) // uses LocalDate.now()

        val todayRecords = repository.getTodayRecords()
        assertEquals(2, todayRecords.size)
        todayRecords.forEach {
            assertEquals(LocalDate.now(), it.date)
        }
    }

    // ─── getRecordsForDate ──────────────────────────────────────────────────

    @Test
    fun `test getRecordsForDate should filter by specific date`() {
        // Add records (all dated today via addRecord)
        repository.addRecord(200)
        repository.addRecord(300)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val todayRecords = repository.getRecordsForDate(today)
        assertEquals(2, todayRecords.size)

        val yesterdayRecords = repository.getRecordsForDate(yesterday)
        assertTrue(yesterdayRecords.isEmpty())
    }

    // ─── getRecordsInRange ─────────────────────────────────────────────────

    @Test
    fun `test getRecordsInRange should include records within date range`() {
        repository.addRecord(200)
        repository.addRecord(300)

        val today = LocalDate.now()
        val allInRange = repository.getRecordsInRange(today.minusDays(1), today.plusDays(1))
        assertEquals(2, allInRange.size)
    }

    @Test
    fun `test getRecordsInRange when start equals end with matching record should include it`() {
        repository.addRecord(200)

        val today = LocalDate.now()
        val inRange = repository.getRecordsInRange(today, today)
        assertEquals(1, inRange.size)
    }

    @Test
    fun `test getRecordsInRange when start equals end with no matching record should be empty`() {
        repository.addRecord(200)

        val yesterday = LocalDate.now().minusDays(1)
        val inRange = repository.getRecordsInRange(yesterday, yesterday)
        assertTrue(inRange.isEmpty())
    }

    @Test
    fun `test getRecordsInRange when range is before all records should be empty`() {
        repository.addRecord(200)

        val today = LocalDate.now()
        val farPast = today.minusDays(10)
        val pastRange = repository.getRecordsInRange(farPast, today.minusDays(2))
        assertTrue(pastRange.isEmpty())
    }

    @Test
    fun `test getRecordsInRange when range is after all records should be empty`() {
        repository.addRecord(200)

        val today = LocalDate.now()
        val farFuture = today.plusDays(10)
        val futureRange = repository.getRecordsInRange(today.plusDays(2), farFuture)
        assertTrue(futureRange.isEmpty())
    }

    // ─── getAllRecords ─────────────────────────────────────────────────────

    @Test
    fun `test getAllRecords should return empty list initially`() {
        assertTrue(repository.getAllRecords().isEmpty())
    }

    @Test
    fun `test getAllRecords should return all records in insertion order`() {
        val r1 = repository.addRecord(200)
        val r2 = repository.addRecord(300)
        val r3 = repository.addRecord(500)

        val all = repository.getAllRecords()
        assertEquals(3, all.size)
        assertEquals(listOf(r1, r2, r3), all)
    }
}
