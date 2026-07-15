package com.alexwater

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for progress calculation logic used in HomeViewModel:
 *   progress = (ml.toFloat() / dailyGoalMl).coerceIn(0f, 1f)
 *   isComplete = progress >= 1f
 */
class ProgressCalculationTest {

    private val defaultGoal = 2000

    // ─── Progress formula ──────────────────────────────────────────────────

    private fun computeProgress(ml: Int, goal: Int): Float {
        if (goal <= 0) return 0f
        return (ml.toFloat() / goal).coerceIn(0f, 1f)
    }

    private fun isComplete(progress: Float): Boolean = progress >= 1f

    @Test
    fun `zero ml returns zero progress`() {
        assertEquals(0f, computeProgress(0, defaultGoal))
    }

    @Test
    fun `exactly half goal returns point five`() {
        assertEquals(0.5f, computeProgress(1000, defaultGoal))
    }

    @Test
    fun `exactly at goal returns one point zero`() {
        assertEquals(1.0f, computeProgress(2000, defaultGoal))
    }

    @Test
    fun `over goal is coerced to one point zero`() {
        assertEquals(1.0f, computeProgress(2500, defaultGoal))
        assertEquals(1.0f, computeProgress(3000, defaultGoal))
        assertEquals(1.0f, computeProgress(9999, defaultGoal))
    }

    @Test
    fun `negative ml results in zero progress via coerceIn`() {
        // Even if somehow negative ml is passed, coerceIn clamps to 0
        val progress = (-500f / defaultGoal).coerceIn(0f, 1f)
        assertEquals(0f, progress)
    }

    @Test
    fun `small amount gives correct fractional progress`() {
        // 200ml of 2000ml goal = 0.1
        assertEquals(0.1f, computeProgress(200, defaultGoal))
    }

    @Test
    fun `one ml of large goal is non-zero but small`() {
        val p = computeProgress(1, 10000)
        assertTrue(p > 0f)
        assertTrue(p < 0.01f)
    }

    // ─── isComplete ────────────────────────────────────────────────────────

    @Test
    fun `progress at zero is not complete`() {
        assertFalse(isComplete(0f))
    }

    @Test
    fun `progress at point nine nine is not complete`() {
        assertFalse(isComplete(0.99f))
    }

    @Test
    fun `progress at exactly one is complete`() {
        assertTrue(isComplete(1.0f))
    }

    @Test
    fun `progress over one is complete`() {
        assertTrue(isComplete(1.5f))
    }

    // ─── Edge cases for goal ───────────────────────────────────────────────

    @Test
    fun `goal of 500ml with 250ml is 0_5`() {
        assertEquals(0.5f, computeProgress(250, 500))
    }

    @Test
    fun `goal of 1000ml with 1000ml is 1_0`() {
        assertEquals(1.0f, computeProgress(1000, 1000))
    }

    @Test
    fun `goal of 1ml with 1ml is 1_0`() {
        assertEquals(1.0f, computeProgress(1, 1))
    }

    @Test
    fun `goal of 1ml with 0ml is 0`() {
        assertEquals(0f, computeProgress(0, 1))
    }

    @Test
    fun `very large goal with zero ml is zero`() {
        assertEquals(0f, computeProgress(0, Int.MAX_VALUE))
    }

    // ─── Float precision / rounding ────────────────────────────────────────

    @Test
    fun `float precision - typical cup amounts`() {
        // 200ml out of 2000ml = exactly 0.1
        val p = computeProgress(200, 2000)
        assertTrue(p > 0.099f && p < 0.101f)
    }

    @Test
    fun `one third of goal gives roughly one third`() {
        // 666/2000 ≈ 0.333
        val p = computeProgress(666, 2000)
        assertTrue(p > 0.33f && p < 0.34f)
    }

    // ─── Progress before/after goal boundary ───────────────────────────────

    @Test
    fun `one ml below goal is less than one`() {
        val p = computeProgress(1999, 2000)
        assertTrue(p < 1.0f)
        assertTrue(p > 0.99f)
    }

    @Test
    fun `one ml above goal is clamped to one`() {
        assertEquals(1.0f, computeProgress(2001, 2000))
    }

    // ─── Realistic scenario from HomeViewModel ────────────────────────────

    @Test
    fun `drinking sequence simulates progress accumulation`() {
        // Simulate: user takes 4 cups of 200ml each, then 300ml
        val cups = listOf(200, 200, 200, 200, 300)
        var cumulativeMl = 0
        val progressValues = mutableListOf<Float>()

        for (cup in cups) {
            cumulativeMl += cup
            progressValues.add(computeProgress(cumulativeMl, defaultGoal))
        }

        // After each cup:
        assertEquals(0.1f, progressValues[0])  // 200/2000
        assertEquals(0.2f, progressValues[1])  // 400/2000
        assertEquals(0.3f, progressValues[2])  // 600/2000
        assertEquals(0.4f, progressValues[3])  // 800/2000
        assertEquals(0.55f, progressValues[4]) // 1100/2000

        // isComplete only after all cups still false
        assertFalse(isComplete(progressValues.last()))
    }

    @Test
    fun `reaching goal exactly after several cups`() {
        val cups = listOf(500, 500, 500, 500) // total = 2000
        var cumulativeMl = 0
        var finalProgress = 0f

        for (cup in cups) {
            cumulativeMl += cup
            finalProgress = computeProgress(cumulativeMl, defaultGoal)
        }

        assertEquals(1.0f, finalProgress)
        assertTrue(isComplete(finalProgress))
    }

    @Test
    fun `exceeding goal after several cups still shows complete`() {
        val cups = listOf(500, 500, 500, 500, 200) // total = 2200
        var cumulativeMl = 0
        var finalProgress = 0f

        for (cup in cups) {
            cumulativeMl += cup
            finalProgress = computeProgress(cumulativeMl, defaultGoal)
        }

        assertEquals(1.0f, finalProgress)
        assertTrue(isComplete(finalProgress))
    }
}
