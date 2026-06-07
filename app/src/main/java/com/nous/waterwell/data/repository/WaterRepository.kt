package com.nous.waterwell.data.repository

import com.nous.waterwell.data.database.DrinkDao
import com.nous.waterwell.data.model.DrinkRecord
import com.nous.waterwell.data.model.SchedulePreset
import com.nous.waterwell.data.model.SchedulePresets
import kotlinx.coroutines.flow.Flow

class WaterRepository(
    private val drinkDao: DrinkDao,
    private val preferencesManager: PreferencesManager
) {
    val preferences: Flow<UserPreferences> = preferencesManager.preferences

    fun getTodayRecords(): Flow<List<DrinkRecord>> = drinkDao.getTodayRecords()
    fun getTodayTotalFlow(): Flow<Int> = drinkDao.getTodayTotal()
    suspend fun getTodayTotalNow(): Int = drinkDao.getTodayTotalNow()

    suspend fun logDrink(amountMl: Int, note: String? = null): Long =
        drinkDao.insert(DrinkRecord(amountMl = amountMl, note = note))

    suspend fun deleteRecord(id: Long) = drinkDao.deleteById(id)
    suspend fun resetToday() = drinkDao.deleteToday()

    fun getAllPresets(): List<SchedulePreset> = SchedulePresets.ALL

    suspend fun selectPreset(presetId: String) = preferencesManager.selectPreset(presetId)
    suspend fun updateTargetMl(ml: Int) = preferencesManager.updateTargetMl(ml)
    suspend fun updateWakeTime(hour: Int, minute: Int) = preferencesManager.updateWakeTime(hour, minute)
    suspend fun updateSleepTime(hour: Int, minute: Int) = preferencesManager.updateSleepTime(hour, minute)
    suspend fun updateCupSize(ml: Int) = preferencesManager.updateCupSize(ml)
    suspend fun setRemindersEnabled(enabled: Boolean) = preferencesManager.setRemindersEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = preferencesManager.setVibrationEnabled(enabled)
    suspend fun setSoundEnabled(enabled: Boolean) = preferencesManager.setSoundEnabled(enabled)
    suspend fun setDarkMode(enabled: Boolean) = preferencesManager.setDarkMode(enabled)
    suspend fun setAccentColorIndex(index: Int) = preferencesManager.setAccentColorIndex(index)
    suspend fun setLanguageCode(code: String) = preferencesManager.setLanguageCode(code)
}
