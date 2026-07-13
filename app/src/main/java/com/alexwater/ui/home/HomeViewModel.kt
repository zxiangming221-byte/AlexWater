package com.alexwater.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.data.AppRepository
import com.alexwater.model.WaterRecord
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository = (application as AlexWaterApp).repository

    private val _todayMl = MutableStateFlow(0)
    val todayMl: StateFlow<Int> = _todayMl.asStateFlow()

    private val _todayRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val todayRecords: StateFlow<List<WaterRecord>> = _todayRecords.asStateFlow()

    val settings: StateFlow<com.alexwater.model.AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.alexwater.model.AppSettings())

    val progress: StateFlow<Float> = combine(_todayMl, settings) { ml, s ->
        (ml.toFloat() / s.dailyGoalMl).coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    val isComplete: StateFlow<Boolean> = progress.map { it >= 1f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        refresh()
    }

    fun addWater(amountMl: Int) {
        repository.addRecord(amountMl)
        refresh()
    }

    fun deleteRecord(record: WaterRecord) {
        repository.deleteRecord(record)
        refresh()
    }

    private fun refresh() {
        val today = LocalDate.now()
        val records = repository.getRecordsForDate(today)
        _todayRecords.value = records.sortedByDescending { it.timestamp }
        _todayMl.value = records.sumOf { it.amountMl }
    }
}
