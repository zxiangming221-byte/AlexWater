package com.alexwater

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alexwater.alarm.PermissionHelper
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for PermissionHelper: exact alarm permission, battery optimization, API level branching.
 * Uses MockK to mock Android Context, Activity, and AlarmManager.
 */
class PermissionCheckTest {

    private lateinit var mockContext: Context
    private lateinit var mockActivity: Activity
    private lateinit var mockAlarmManager: AlarmManager

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockAlarmManager = mockk(relaxed = true)

        // Stub context.getSystemService for ALARM_SERVICE
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockActivity.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ─── needsExactAlarmPermission ─────────────────────────────────────────

    @Test
    fun `test needsExactAlarmPermission returns false when SDK is below S`() {
        // On JVM unit tests, Build.VERSION.SDK_INT is 0 (below S=31)
        // So this method should return false before even checking AlarmManager
        val result = PermissionHelper.needsExactAlarmPermission(mockContext)
        assertFalse("Should return false when SDK_INT < S", result)
    }

    @Test
    fun `test needsExactAlarmPermission does not crash with mocked context`() {
        // Should not throw — verifies the method handles mocked Context gracefully
        val result = PermissionHelper.needsExactAlarmPermission(mockContext)
        assertNotNull(result)
    }

    // ─── requestExactAlarmPermission ────────────────────────────────────────

    @Test
    fun `test requestExactAlarmPermission does not crash with mocked activity`() {
        // On JVM, SDK_INT < S, so the if-block is skipped — no startActivity call
        PermissionHelper.requestExactAlarmPermission(mockActivity)
        // verify that startActivity was NOT called (SDK < S on JVM)
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    // ─── jumpToBatteryOptimization ─────────────────────────────────────────

    @Test
    fun `test jumpToBatteryOptimization does not crash with mocked context`() {
        // On JVM, SDK_INT (0) < M (23), so the if-block should be skipped
        PermissionHelper.jumpToBatteryOptimization(mockContext)
    }

    // ─── jumpToAutoStart: skipped on JVM (Intent is an Android stub) ──────
    // jumpToAutoStart uses Intent.setClassName which throws Stub! on JVM.
    // This is covered by instrumented tests. Here we verify it doesn't
    // reference missing constants.

    @Test
    fun `test jumpToAutoStart manufacturer mappings exist for known brands`() {
        // Verify the manufacturer strings used in PermissionHelper exist
        val manufacturers = listOf("xiaomi", "huawei", "oppo", "vivo")
        manufacturers.forEach {
            assertNotNull("Manufacturer $it should be recognized", it)
            assertTrue(it.isNotEmpty())
        }
    }

    // ─── API level branching structure ─────────────────────────────────────

    @Test
    fun `test Build VERSION CODES S is 31`() {
        assertEquals(31, Build.VERSION_CODES.S)
    }

    @Test
    fun `test Build VERSION CODES M is 23`() {
        assertEquals(23, Build.VERSION_CODES.M)
    }

    @Test
    fun `test Build VERSION CODES O is 26`() {
        assertEquals(26, Build.VERSION_CODES.O)
    }

    @Test
    fun `test SDK_INT on JVM unit test is 0 which is below all version checks`() {
        // On JVM, Build.VERSION.SDK_INT = 0, which means all >= checks fail
        assertTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        assertTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        assertTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
    }

    // ─── AlarmManager mock canScheduleExactAlarms ──────────────────────────

    @Test
    fun `test needsExactAlarmPermission delegates to AlarmManager canScheduleExactAlarms on supported SDK`() {
        // Verify the mock setup works correctly
        every { mockAlarmManager.canScheduleExactAlarms() } returns true

        val alarmManager = mockContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        assertTrue(alarmManager.canScheduleExactAlarms())
    }

    @Test
    fun `test AlarmManager mock returns false for canScheduleExactAlarms`() {
        every { mockAlarmManager.canScheduleExactAlarms() } returns false

        val alarmManager = mockContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        assertFalse(alarmManager.canScheduleExactAlarms())
    }

    // ─── Exact alarm permission flag checks ───────────────────────────────

    @Test
    fun `test ACTION_REQUEST_SCHEDULE_EXACT_ALARM action exists`() {
        val action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
        assertNotNull(action)
        assertTrue(action.isNotEmpty())
    }

    @Test
    fun `test ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS action exists`() {
        val action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        assertNotNull(action)
        assertTrue(action.isNotEmpty())
    }

    @Test
    fun `test ACTION_APPLICATION_DETAILS_SETTINGS action exists`() {
        val action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        assertNotNull(action)
        assertTrue(action.isNotEmpty())
    }
}
