package com.alexwater.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.alarm.AlarmScheduler
import com.alexwater.alarm.CalendarReminderManager
import com.alexwater.model.DndPeriod
import com.alexwater.model.ReminderConfig
import com.alexwater.model.ReminderPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as AlexWaterApp).repository

    val config: StateFlow<ReminderConfig> = repo.reminderConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderConfig())

    fun updateEnabled(enabled: Boolean) {
        update { it.copy(enabled = enabled) }
    }

    fun updatePlan(plan: ReminderPlan) {
        update { it.copy(plan = plan) }
    }


    fun updateCustomInterval(minutes: Int) {
        update { it.copy(plan = ReminderPlan.CUSTOM_INTERVAL, customIntervalMinutes = minutes) }
    }

    fun addCustomTime(time: LocalTime) {
        update { it.copy(customTimes = (it.customTimes + time).sortedBy { t -> t }) }
    }

    fun removeCustomTime(time: LocalTime) {
        val newTimes = config.value.customTimes - time
        if (newTimes.isEmpty()) {
            // Fall back to hourly plan when all times removed
            update { it.copy(plan = ReminderPlan.HOURLY, customTimes = emptyList()) }
        } else {
            update { it.copy(customTimes = newTimes) }
        }
    }

    fun addDndPeriod(period: DndPeriod) {
        update { it.copy(dndPeriods = it.dndPeriods + period) }
    }

    fun editDndPeriod(oldPeriod: DndPeriod, newPeriod: DndPeriod) {
        update { config ->
            val list = config.dndPeriods.toMutableList()
            val idx = list.indexOf(oldPeriod)
            if (idx >= 0) list[idx] = newPeriod
            config.copy(dndPeriods = list)
        }
    }

    fun removeDndPeriod(period: DndPeriod) {
        update { it.copy(dndPeriods = it.dndPeriods - period) }
    }

    fun updateDndEnabled(enabled: Boolean) {
        update { it.copy(dndEnabled = enabled) }
    }

    private fun update(transform: (ReminderConfig) -> ReminderConfig) {
        val newConfig = transform(config.value)
        viewModelScope.launch {
            repo.updateReminderConfig(newConfig)
            withContext(Dispatchers.IO) {
                AlarmScheduler.scheduleReminders(getApplication(), newConfig)
                CalendarReminderManager.sync(getApplication(), newConfig)
            }
        }
    }
}
