package com.nous.waterwell.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nous.waterwell.data.database.AppDatabase
import com.nous.waterwell.data.model.DrinkRecord
import com.nous.waterwell.data.model.SchedulePreset
import com.nous.waterwell.data.model.SchedulePresets
import com.nous.waterwell.data.repository.PreferencesManager
import com.nous.waterwell.data.repository.UserPreferences
import com.nous.waterwell.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WaterRepository

    val preferences: StateFlow<UserPreferences>
    val todayTotal: StateFlow<Int>
    val todayRecords: StateFlow<List<DrinkRecord>>
    val allPresets: List<SchedulePreset> = SchedulePresets.ALL

    private val _selectedPreset = MutableStateFlow<SchedulePreset?>(null)
    val selectedPreset: StateFlow<SchedulePreset?> = _selectedPreset.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        val prefsManager = PreferencesManager(application)
        repository = WaterRepository(db.drinkDao(), prefsManager)

        preferences = repository.preferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

        todayTotal = repository.getTodayTotalFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

        todayRecords = repository.getTodayRecords()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        viewModelScope.launch {
            preferences.collect { prefs ->
                _selectedPreset.value = SchedulePresets.ALL.find { it.id == prefs.activePresetId }
            }
        }
    }

    fun refreshToday() {
        viewModelScope.launch {
            repository.getTodayTotalNow()
        }
    }

    fun logDrink(amountMl: Int, note: String? = null) {
        viewModelScope.launch { repository.logDrink(amountMl, note) }
    }

    fun deleteRecord(id: Long) { viewModelScope.launch { repository.deleteRecord(id) } }
    fun resetToday() { viewModelScope.launch { repository.resetToday() } }
    fun selectPreset(presetId: String) { viewModelScope.launch { repository.selectPreset(presetId) } }
    fun updateTargetMl(ml: Int) { viewModelScope.launch { repository.updateTargetMl(ml) } }
    fun updateWakeTime(hour: Int, minute: Int) { viewModelScope.launch { repository.updateWakeTime(hour, minute) } }
    fun updateSleepTime(hour: Int, minute: Int) { viewModelScope.launch { repository.updateSleepTime(hour, minute) } }
    fun updateCupSize(ml: Int) { viewModelScope.launch { repository.updateCupSize(ml) } }
    fun setRemindersEnabled(enabled: Boolean) { viewModelScope.launch { repository.setRemindersEnabled(enabled) } }
    fun setVibrationEnabled(enabled: Boolean) { viewModelScope.launch { repository.setVibrationEnabled(enabled) } }
    fun setSoundEnabled(enabled: Boolean) { viewModelScope.launch { repository.setSoundEnabled(enabled) } }
    fun setDarkMode(enabled: Boolean) { viewModelScope.launch { repository.setDarkMode(enabled) } }
    fun setLanguageCode(code: String) { viewModelScope.launch { repository.setLanguageCode(code) } }
    fun setAccentColorIndex(index: Int) { viewModelScope.launch { repository.setAccentColorIndex(index) } }
}
