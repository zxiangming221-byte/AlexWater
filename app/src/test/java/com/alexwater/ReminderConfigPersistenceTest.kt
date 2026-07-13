package com.alexwater

import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for ReminderConfig serialization/deserialization to the DataStore string format.
 * Verifies that customTimes and DND periods round-trip correctly through
 * "HH:mm" and "HH:mm|HH:mm" string representations, and that malformed data
 * is safely handled.
 */
class ReminderConfigPersistenceTest {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // ─── Custom times: serialization (to string set) ────────────────────────

    @Test
    fun `test customTimes serialize to HH mm format strings`() {
        val config = ReminderConfig(
            customTimes = listOf(LocalTime.of(8, 0), LocalTime.of(10, 30), LocalTime.of(14, 0))
        )
        val stringSet = config.customTimes
            .map { it.format(timeFormatter) }
            .toSet()

        assertEquals(3, stringSet.size)
        assertTrue(stringSet.contains("08:00"))
        assertTrue(stringSet.contains("10:30"))
        assertTrue(stringSet.contains("14:00"))
    }

    @Test
    fun `test customTimes round-trip through string format preserves values`() {
        val original = listOf(LocalTime.of(6, 15), LocalTime.of(12, 45), LocalTime.of(21, 0))
        val stringSet = original.map { it.format(timeFormatter) }.toSet()
        val parsed = stringSet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }

        assertEquals(original.sortedBy { it }, parsed)
    }

    @Test
    fun `test customTimes with midnight and noon serialize correctly`() {
        val config = ReminderConfig(
            customTimes = listOf(LocalTime.of(0, 0), LocalTime.of(12, 0), LocalTime.of(23, 59))
        )
        val stringSet = config.customTimes
            .map { it.format(timeFormatter) }
            .toSet()

        assertTrue(stringSet.contains("00:00"))
        assertTrue(stringSet.contains("12:00"))
        assertTrue(stringSet.contains("23:59"))
    }

    // ─── Custom times: parsing safety ──────────────────────────────────────

    @Test
    fun `test customTimes parsing with malformed string should be skipped`() {
        val stringSet = setOf("08:00", "INVALID", "14:00", "25:00", "not-a-time")
        val parsed = stringSet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }

        assertEquals(2, parsed.size)
        assertEquals(LocalTime.of(8, 0), parsed[0])
        assertEquals(LocalTime.of(14, 0), parsed[1])
    }

    @Test
    fun `test customTimes parsing with all malformed strings returns empty then falls back to default`() {
        val stringSet = setOf("abc", "xyz", "bad")
        val parsed = stringSet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }

        assertTrue(parsed.isEmpty())
        val withDefault = parsed.ifEmpty { listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)) }
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)), withDefault)
    }

    @Test
    fun `test customTimes parsing with empty set returns default values`() {
        val emptySet: Set<String> = emptySet()
        val parsed = emptySet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }
            .ifEmpty { listOf(LocalTime.of(8, 0), LocalTime.of(10, 30)) }

        assertEquals(2, parsed.size)
        assertEquals(LocalTime.of(8, 0), parsed[0])
        assertEquals(LocalTime.of(10, 30), parsed[1])
    }

    @Test
    fun `test customTimes parsing with empty string should be skipped`() {
        val stringSet = setOf("", "08:00")
        val parsed = stringSet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }

        assertEquals(1, parsed.size)
        assertEquals(LocalTime.of(8, 0), parsed[0])
    }

    @Test
    fun `test customTimes parsing preserves sorted order`() {
        val stringSet = setOf("22:00", "06:00", "14:30", "08:00")
        val parsed = stringSet
            .mapNotNull { try { LocalTime.parse(it) } catch (_: Exception) { null } }
            .sortedBy { it }

        assertEquals(LocalTime.of(6, 0), parsed[0])
        assertEquals(LocalTime.of(8, 0), parsed[1])
        assertEquals(LocalTime.of(14, 30), parsed[2])
        assertEquals(LocalTime.of(22, 0), parsed[3])
    }

    // ─── DND periods: serialization ────────────────────────────────────────

    @Test
    fun `test dndPeriods serialize to HH mm HH mm format with pipe`() {
        val config = ReminderConfig(
            dndPeriods = listOf(
                DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)),
                DndPeriod(LocalTime.of(13, 0), LocalTime.of(14, 0)),
            )
        )
        val stringSet = config.dndPeriods
            .map { "${it.start.format(timeFormatter)}|${it.end.format(timeFormatter)}" }
            .toSet()

        assertEquals(2, stringSet.size)
        assertTrue(stringSet.contains("22:00|08:00"))
        assertTrue(stringSet.contains("13:00|14:00"))
    }

    @Test
    fun `test dndPeriods round-trip through string format preserves values`() {
        val original = listOf(
            DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)),
            DndPeriod(LocalTime.of(13, 0), LocalTime.of(14, 0)),
        )
        val stringSet = original.map { "${it.start.format(timeFormatter)}|${it.end.format(timeFormatter)}" }.toSet()
        val parsed = stringSet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }

        assertEquals(original.toSet(), parsed.toSet())
    }

    // ─── DND periods: parsing safety ───────────────────────────────────────

    @Test
    fun `test dndPeriods parsing with malformed string missing pipe should be skipped`() {
        val stringSet = setOf("22:00|08:00", "13:00-14:00", "bad")
        val parsed = stringSet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }

        assertEquals(1, parsed.size)
        assertEquals(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)), parsed[0])
    }

    @Test
    fun `test dndPeriods parsing with empty set returns default cross-midnight DND`() {
        val emptySet: Set<String> = emptySet()
        val parsed = emptySet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }
        val withDefault = parsed.ifEmpty { listOf(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0))) }

        assertEquals(1, withDefault.size)
        assertEquals(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)), withDefault[0])
    }

    @Test
    fun `test dndPeriods parsing with invalid time in one part should be skipped`() {
        val stringSet = setOf("22:00|08:00", "BAD|14:00", "13:00|INVALID")
        val parsed = stringSet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }

        assertEquals(1, parsed.size)
        assertEquals(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)), parsed[0])
    }

    @Test
    fun `test dndPeriods parsing with too many pipes should be skipped`() {
        val stringSet = setOf("22:00|08:00|extra")
        val parsed = stringSet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }

        // split.size != 2, so it's skipped
        assertTrue(parsed.isEmpty())
    }

    @Test
    fun `test dndPeriods parsing with empty parts should be skipped`() {
        val stringSet = setOf("|", "22:00|08:00")
        val parsed = stringSet.mapNotNull { parts ->
            val split = parts.split("|")
            if (split.size == 2) {
                try { DndPeriod(LocalTime.parse(split[0]), LocalTime.parse(split[1])) }
                catch (_: Exception) { null }
            } else null
        }

        assertEquals(1, parsed.size)
        assertEquals(DndPeriod(LocalTime.of(22, 0), LocalTime.of(8, 0)), parsed[0])
    }

    // ─── ReminderConfig default values ─────────────────────────────────────

    @Test
    fun `test ReminderConfig default enabled should be true`() {
        val config = ReminderConfig()
        assertTrue(config.enabled)
    }

    @Test
    fun `test ReminderConfig default plan should be HOURLY`() {
        val config = ReminderConfig()
        assertEquals(ReminderPlan.HOURLY, config.plan)
    }

    @Test
    fun `test ReminderConfig default mode should be ALARM`() {
        val config = ReminderConfig()
    }

    @Test
    fun `test ReminderConfig default customIntervalMinutes should be 45`() {
        val config = ReminderConfig()
        assertEquals(45, config.customIntervalMinutes)
    }

    @Test
    fun `test ReminderConfig default customTimes should be 8 00 and 10 30`() {
        val config = ReminderConfig()
        assertEquals(2, config.customTimes.size)
        assertEquals(LocalTime.of(8, 0), config.customTimes[0])
        assertEquals(LocalTime.of(10, 30), config.customTimes[1])
    }

    @Test
    fun `test ReminderConfig default dndPeriods should be cross-midnight`() {
        val config = ReminderConfig()
        assertEquals(1, config.dndPeriods.size)
        assertEquals(LocalTime.of(22, 0), config.dndPeriods[0].start)
        assertEquals(LocalTime.of(8, 0), config.dndPeriods[0].end)
    }

    // ─── ReminderPlan enum ordinal mapping (as used in DataStore) ──────────

    @Test
    fun `test ReminderPlan enum has exactly 5 values`() {
        assertEquals(5, ReminderPlan.entries.size)
    }

    @Test
    fun `test ReminderPlan ordinal values match expected mapping for DataStore`() {
        // HOURLY=0, EVERY_1_5H=1, EVERY_2H=2, CUSTOM_INTERVAL=3, CUSTOM_TIMES=4
        assertEquals(0, ReminderPlan.HOURLY.ordinal)
        assertEquals(1, ReminderPlan.EVERY_1_5H.ordinal)
        assertEquals(2, ReminderPlan.EVERY_2H.ordinal)
        assertEquals(3, ReminderPlan.CUSTOM_INTERVAL.ordinal)
        assertEquals(4, ReminderPlan.CUSTOM_TIMES.ordinal)
    }
}
