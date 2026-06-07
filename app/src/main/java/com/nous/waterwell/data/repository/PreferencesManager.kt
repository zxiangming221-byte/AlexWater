package com.nous.waterwell.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "waterwell_prefs")

data class UserPreferences(
    val targetMl: Int = 2000,
    val wakeUpHour: Int = 7,
    val wakeUpMinute: Int = 0,
    val sleepHour: Int = 22,
    val sleepMinute: Int = 0,
    val cupSizeMl: Int = 250,
    val activePresetId: String = "standard_8",
    val remindersEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val accentColorIndex: Int = 0,  // index into AccentPresets
    val languageCode: String = "zh-CN"  // "en", "zh-CN", "zh-TW"
)

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_TARGET_ML = intPreferencesKey("target_ml")
        private val KEY_WAKE_HOUR = intPreferencesKey("wake_hour")
        private val KEY_WAKE_MINUTE = intPreferencesKey("wake_minute")
        private val KEY_SLEEP_HOUR = intPreferencesKey("sleep_hour")
        private val KEY_SLEEP_MINUTE = intPreferencesKey("sleep_minute")
        private val KEY_CUP_SIZE = intPreferencesKey("cup_size_ml")
        private val KEY_ACTIVE_PRESET = stringPreferencesKey("active_preset_id")
        private val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val KEY_VIBRATION = booleanPreferencesKey("vibration_enabled")
        private val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_ACCENT_INDEX = intPreferencesKey("accent_color_index")
        private val KEY_LANGUAGE = stringPreferencesKey("language_code")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            targetMl = prefs[KEY_TARGET_ML] ?: 2000,
            wakeUpHour = prefs[KEY_WAKE_HOUR] ?: 7,
            wakeUpMinute = prefs[KEY_WAKE_MINUTE] ?: 0,
            sleepHour = prefs[KEY_SLEEP_HOUR] ?: 22,
            sleepMinute = prefs[KEY_SLEEP_MINUTE] ?: 0,
            cupSizeMl = prefs[KEY_CUP_SIZE] ?: 250,
            activePresetId = prefs[KEY_ACTIVE_PRESET] ?: "standard_8",
            remindersEnabled = prefs[KEY_REMINDERS_ENABLED] ?: true,
            vibrationEnabled = prefs[KEY_VIBRATION] ?: true,
            soundEnabled = prefs[KEY_SOUND] ?: true,
            darkMode = prefs[KEY_DARK_MODE] ?: false,
            accentColorIndex = prefs[KEY_ACCENT_INDEX] ?: 0,
            languageCode = prefs[KEY_LANGUAGE] ?: "zh-CN"
        )
    }

    suspend fun updateTargetMl(ml: Int) { context.dataStore.edit { it[KEY_TARGET_ML] = ml } }
    suspend fun updateWakeTime(hour: Int, minute: Int) {
        context.dataStore.edit { it[KEY_WAKE_HOUR] = hour; it[KEY_WAKE_MINUTE] = minute }
    }
    suspend fun updateSleepTime(hour: Int, minute: Int) {
        context.dataStore.edit { it[KEY_SLEEP_HOUR] = hour; it[KEY_SLEEP_MINUTE] = minute }
    }
    suspend fun updateCupSize(ml: Int) { context.dataStore.edit { it[KEY_CUP_SIZE] = ml } }
    suspend fun selectPreset(presetId: String) { context.dataStore.edit { it[KEY_ACTIVE_PRESET] = presetId } }
    suspend fun setRemindersEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_REMINDERS_ENABLED] = enabled } }
    suspend fun setVibrationEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_VIBRATION] = enabled } }
    suspend fun setSoundEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_SOUND] = enabled } }
    suspend fun setDarkMode(enabled: Boolean) { context.dataStore.edit { it[KEY_DARK_MODE] = enabled } }
    suspend fun setAccentColorIndex(index: Int) { context.dataStore.edit { it[KEY_ACCENT_INDEX] = index } }
    suspend fun setLanguageCode(code: String) { context.dataStore.edit { it[KEY_LANGUAGE] = code } }
}
